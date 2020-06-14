package com.lennoardsilva.androidmobillschallenge.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.card.MaterialCardView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.lennoardsilva.androidmobillschallenge.R
import com.lennoardsilva.androidmobillschallenge.data.model.Attachment
import com.lennoardsilva.androidmobillschallenge.sheets.ContextBottomSheet
import com.lennoardsilva.androidmobillschallenge.sheets.SheetOption
import com.lennoardsilva.androidmobillschallenge.utils.Utils

interface AttachmentRequestListener {
    fun onDownloadRequested(attachment: Attachment)
    fun onDeleteRequested(attachment: Attachment)
}


class AttachmentAdapter(
    private val dataSet: MutableList<Attachment>,
    private val context: Context,
    private val onAttachmentRequestListener: AttachmentRequestListener? = null
) : RecyclerView.Adapter<AttachmentAdapter.ViewHolder>() {

    private val storage : StorageReference by lazy {
        FirebaseStorage.getInstance().reference
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var name: TextView = v.findViewById(R.id.attachmentItemFileName)
        var fileIcon: ImageView = v.findViewById(R.id.attachmentItemFileType)
        var image: ImageView = v.findViewById(R.id.attachmentItemImage)
        var itemLayout: MaterialCardView = v.findViewById(R.id.attachmentItemCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(context).inflate(
            R.layout.list_item_attachment,
            parent,
            false
        )
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val attachment = dataSet[position]
        val ref = storage.child(attachment.storagePath)

        holder.name.text = ref.name

        ref.metadata.addOnSuccessListener {
            holder.fileIcon.setImageResource(Utils.getIconFromContentType(it.contentType!!))
            if (it.contentType!!.startsWith("image/")) {
                Glide.with(context)
                    .load(attachment.downloadURL)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.broken_image)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(holder.image)
            }
        }

        holder.itemLayout.setOnClickListener {
            val options = arrayListOf(
                SheetOption(
                    context.getString(R.string.download),
                    "download",
                    icon = R.drawable.ic_download
                ),
                SheetOption(
                    context.getString(R.string.delete),
                    "delete",
                    R.color.colorError,
                    R.drawable.ic_delete
                )
            )
            val sheet = ContextBottomSheet.newInstance(ref.name, options)
            sheet.onOptionClickListener = object : ContextBottomSheet.OnOptionClickListener {
                override fun onOptionClick(tag: String) {
                    when (tag) {
                        "delete" -> {
                            deleteAttachment(attachment)
                            sheet.dismiss()
                        }

                        "download" -> {
                            downloadAttachment(attachment)
                            sheet.dismiss()
                        }
                    }
                }
            }
            sheet.show((context as AppCompatActivity).supportFragmentManager, "sheet")
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    fun updateData(newData: List<Attachment>) {
        dataSet.clear()
        dataSet.addAll(newData)
        notifyDataSetChanged()
    }

    private fun deleteAttachment(attachment: Attachment) {
        onAttachmentRequestListener?.onDeleteRequested(attachment)
    }

    private fun downloadAttachment(attachment: Attachment){
        onAttachmentRequestListener?.onDownloadRequested(attachment)
    }
}
