package com.lennoardsilva.androidmobillschallenge.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.lennoardsilva.androidmobillschallenge.BillsApp
import com.lennoardsilva.androidmobillschallenge.R
import com.lennoardsilva.androidmobillschallenge.adapters.AttachmentAdapter
import com.lennoardsilva.androidmobillschallenge.adapters.AttachmentRequestListener
import com.lennoardsilva.androidmobillschallenge.data.model.Attachment
import com.lennoardsilva.androidmobillschallenge.data.model.Expense
import com.lennoardsilva.androidmobillschallenge.data.model.Revenue
import com.lennoardsilva.androidmobillschallenge.data.model.Transaction
import com.lennoardsilva.androidmobillschallenge.timeString
import com.lennoardsilva.androidmobillschallenge.toast
import com.lennoardsilva.androidmobillschallenge.utils.*
import kotlinx.android.synthetic.main.activity_edit_transaction.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

sealed class OperationResult
object Success : OperationResult()
class Failure(val message: String) : OperationResult()

class EditTransactionActivity : BaseActivity(), AttachmentRequestListener {
    private var transaction: Transaction? = null
    private var storagePath : String? = null
    private val storage : StorageReference by lazy {
        FirebaseStorage.getInstance().reference
    }
    private val attachmentAdapter: AttachmentAdapter by lazy {
        AttachmentAdapter(mutableListOf(), this, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_transaction)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        transaction = intent.getSerializableExtra(EXTRA_TRANSACTION) as Transaction?
        if (transaction == null) {
            toast(R.string.error)
            finish()
        } else {
            editTransactionDescription.setText(transaction!!.descricao)
            editTransactionValue.setText(transaction!!.valor.toString())
            editTransactionTime.setText(Utils.dateMillisToString(transaction!!.time))
            editTransactionTime.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    pickDate()
                }
            }

            when (transaction) {
                is Expense -> {
                    editTransactionPaid.setText(R.string.paid)
                    editTransactionPaid.isChecked = (transaction as Expense).pago
                }

                is Revenue -> {
                    editTransactionPaid.setText(R.string.received)
                    editTransactionPaid.isChecked = (transaction as Revenue).recebido
                }
            }

            editTransactionAddAttachment.setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                startActivityForResult(intent, PICK_FILE_FOR_UPLOAD_RC)
            }

            editTransactionDone.setOnClickListener {
                if (validateInputFields()) {
                    saveTransaction { result ->
                        when (result) {
                            is Success -> {
                                toast(R.string.done)
                                finish()
                            }

                            is Failure -> {
                                showErrorDialog(getString(R.string.failure_format, result.message))
                            }
                        }
                    }
                } else {
                    editTransactionDone.snackbar(getString(R.string.check_input_fields))
                }
            }

            editTransactionAttachmentsRecyclerView.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(
                    this.context,
                    RecyclerView.HORIZONTAL,
                    false
                )
                adapter = attachmentAdapter
            }
            attachmentAdapter.updateData(transaction!!.attachments)
        }
    }

    override fun onDownloadRequested(attachment: Attachment) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_TITLE, attachment.name)
        }
        storagePath = attachment.storagePath

        startActivityForResult(intent, SELECT_FILE_FOR_DOWNLOAD_RC)
    }

    override fun onDeleteRequested(attachment: Attachment) {
        AlertDialog.Builder(this)
            .setIcon(R.drawable.ic_error)
            .setTitle(android.R.string.dialog_alert_title)
            .setMessage(R.string.delete_prompt)
            .setNegativeButton(android.R.string.no) { _, _ -> }
            .setPositiveButton(android.R.string.yes) { _, _ ->
                editTransactionProgress.show()

                val ref = storage.child(attachment.storagePath)
                ref.delete().addOnSuccessListener {
                    transaction?.attachments?.remove(attachment)
                    saveAttachments()
                }.addOnFailureListener {
                    showErrorDialog(it.message!!)
                }
            }.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            SELECT_FILE_FOR_DOWNLOAD_RC -> {
                intent?.data?.let { uri ->
                    try {
                        downloadAttachment(uri)
                    } catch (e: Exception) {
                        toast(e.message)
                    }
                }
            }

            PICK_FILE_FOR_UPLOAD_RC -> {
                intent?.data?.let { uri ->
                    try {
                        uploadAttachment(uri)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showErrorDialog(e.message)
                    }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveAttachments() {
        val ref = if (transaction is Expense) {
            BillsApp.userExpensesRef
        } else {
            BillsApp.userRevenuesRef
        }

        editTransactionProgress.show()
        ref.document(transaction!!.id)
            .update(mapOf("attachments" to transaction!!.attachments))
            .addOnCompleteListener { task ->
                editTransactionProgress.hide()
                if (task.isSuccessful) {
                    editTransactionDone.snackbar(getString(R.string.done))
                    attachmentAdapter.updateData(transaction!!.attachments)
                } else {
                    showErrorDialog(task.exception?.message)
                }
            }
    }

    private fun saveTransaction(onResult: (OperationResult) -> Unit) {
        editTransactionProgress.show()

        val ref = if (transaction is Expense) {
            BillsApp.userExpensesRef
        } else {
            BillsApp.userRevenuesRef
        }
        val date = Utils.parseDate(editTransactionTime.text.toString())
        transaction?.apply {
            descricao = editTransactionDescription.text.toString()
            valor = editTransactionValue.text.toString().toDouble()

            date?.let {
                data = Timestamp(date)
                time = date.time
            }
        }

        when (transaction) {
            is Expense -> (transaction as Expense).pago = editTransactionPaid.isChecked
            is Revenue -> (transaction as Revenue).recebido = editTransactionPaid.isChecked
        }

        ref.document(transaction!!.id).set(transaction!!).addOnCompleteListener { task ->
            editTransactionProgress.hide()
            if (task.isSuccessful) {
                onResult(Success)
            } else {
                onResult(Failure(task.exception!!.message!!))
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun pickDate() {
        val calendar = GregorianCalendar.getInstance()
        calendar.time = Date(transaction!!.time)

        val datePicker = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val timePicker = TimePickerDialog(this, { _, hourOfDay, minute ->
                val dateMillis = GregorianCalendar(
                    year, month, dayOfMonth,
                    hourOfDay, minute
                ).timeInMillis

                transaction?.time = dateMillis
                transaction?.data = Timestamp(Date(dateMillis))

                // 01/01/2010 01:01
                editTransactionTime.setText(
                    "${dayOfMonth.timeString()}/${(month+1).timeString()}/$year ${hourOfDay.timeString()}:${minute.timeString()}"
                )
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
            timePicker.show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePicker.show()
    }

    private fun downloadAttachment(uri: Uri) {
        if (storagePath == null)  {
            toast(R.string.error)
            return
        }
        val ref = storage.child(storagePath!!)


        editTransactionProgress.show()
        ref.getBytes(TEN_MEGABYTES).addOnSuccessListener { byteArray ->
            try {
                contentResolver.openFileDescriptor(uri, "w")?.use {
                    FileOutputStream(it.fileDescriptor).use { fileOutputStream ->
                        fileOutputStream.write(byteArray)
                    }
                }
                editTransactionProgress.hide()
                editTransactionDone.snackbar(getString(R.string.download_completed))
                storagePath = null
            } catch (e: Exception) {
                editTransactionProgress.hide()
                showErrorDialog(e.message!!)
            }
        }.addOnFailureListener {
            editTransactionProgress.hide()
            showErrorDialog(it.message!!)
        }
    }

    private fun uploadAttachment(uri: Uri) {
        val id = transaction!!.id
        val path = "${BillsApp.userSpace}/attachments/$id/${Utils.getFilenameFromUri(uri)}"
        val ref = storage.child(path)

        fun performUpload(data: ByteArray) {
            val uploadTask = ref.putBytes(data)
            val attachment = Attachment(
                name = Utils.getFilenameFromUri(uri),
                storagePath = path
            )

            uploadTask.addOnProgressListener { task ->
                val progress: Double = 100.0 * task.bytesTransferred / task.totalByteCount
                editTransactionProgressDeterminate.progress = progress.toInt()
            }.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                ref.downloadUrl
            }.addOnCompleteListener { task ->
                editTransactionProgressDeterminate.hide()
                if (task.isSuccessful) {
                    attachment.downloadURL = task.result.toString()
                    transaction?.attachments?.add(attachment)
                    saveAttachments()
                } else {
                    showErrorDialog(task.exception?.message)
                }
            }
        }

        editTransactionProgressDeterminate.show()
        try {
            contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                FileInputStream(descriptor.fileDescriptor).use { inputStream ->
                    if (inputStream.available() > TEN_MEGABYTES) {
                        throw Exception(getString(R.string.error_file_too_big))
                    }
                    performUpload(inputStream.readBytes())
                }
            }
        } catch (e: Exception) {
            editTransactionProgressDeterminate.hide()
            showErrorDialog(e.message!!)
        }
    }

    private fun validateInputFields() : Boolean {
        if (!editTransactionValue.validateDouble { it >= 0 }) {
            editTransactionValue.error = getString(R.string.invalid_value)
            return false
        }

        if (!editTransactionTime.validateDatetime()) {
            editTransactionTime.error = getString(R.string.invalid_value)
            return false
        }

        if (!editTransactionDescription.validateString { it.isNotEmpty() }) {
            editTransactionDescription.error = getString(R.string.invalid_value)
            return false
        }

        return true
    }
    
    companion object {
        private const val SELECT_FILE_FOR_DOWNLOAD_RC: Int = 1
        private const val PICK_FILE_FOR_UPLOAD_RC: Int = 2
        private const val TEN_MEGABYTES: Long = (10 * 1024) * 1024
        const val EXTRA_TRANSACTION = "transaction"
    }
}
