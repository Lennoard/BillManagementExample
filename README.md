

<p align="center">
  <img height="360" src="https://i.imgur.com/tUYuuQL.jpg"/>
  <img height="360" src="https://i.imgur.com/LOf87HC.jpg"/>
  <img height="360" src="https://i.imgur.com/hXzAfmq.jpg"/>
  <img height="360" src="https://i.imgur.com/3fFUGiO.jpg"/>
</p>

# BillManagementExample

![](https://img.shields.io/github/languages/top/Lennoard/BillManagementExample)

A simple example application to manage your expenses / earnings

### Features

- Manage expenses and earnings
- Sort by month
- Detailed month report
- Upload attachments to each transaction



## Compiling
Recommended Android Studio 4.0+ and Build Tools 29.0.3+

- Clone the repository `git clone https://github.com/Lennoard/BillManagementExample.git`(or [download](https://github.com/Lennoard/BillManagementExample/archive/master.zip) and extract it)
- Create a Firebase Project ([here](https://console.firebase.google.com/))
- Once the project is created, go to the `Project Settings`in the left-hand menu, scroll down and click on download `google-services.json`
- Copy the downloaded file to the this repo's [`app/`](https://github.com/Lennoard/BillManagementExample/tree/master/app) directory, like so:

<p align="center">
  <img height="360" src="https://i.stack.imgur.com/BFmz5.png"/>
</p>

You may need to add SHA certificate fingerprints to the project settings, to do so:
- In Android Studio, click in Gadle (on the right-hand panel)
- Expand `Tasks -> Android`
- Double click `signingReport` to get the debug SHA1 of your machine
- Paste it in firabase project settings, download `google-services.json` again and replace it in the project's app/ folder

For any further information about setting up firebase, please refer to the [official documentation](https://firebase.google.com/docs/android/setup).

### License

> This program is free software: you can redistribute it and/or modify
> it under the terms of the GNU General Public License as published by
> the Free Software Foundation, either version 3 of the License, or
> (at your option) any later version.
> 
> This program is distributed in the hope that it will be useful,
> but WITHOUT ANY WARRANTY; without even the implied warranty of
> MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
> GNU General Public License for more details.
> 
> You should have received a copy of the GNU General Public License
> along with this program.  If not, see <https://www.gnu.org/licenses/>
