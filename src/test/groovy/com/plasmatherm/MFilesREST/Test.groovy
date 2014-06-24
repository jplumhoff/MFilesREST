package com.plasmatherm.MFilesREST


def MFServer = 'M-Files URL'  //your M-Files URL
def MFVault = 'VAULT ID'  //your vault GUID without curly braces
def expirationDate = new Date()
expirationDate = expirationDate+1
def MFiles = new MFilesREST([
        MFBaseURL : MFServer + 'REST/',
        MFLogin : [Username : 'SomeUser',
                Password : 'SomePassword',
                Winuser : true,                 //set to true if you are using windows logon, false if m-files
                Domain : 'SomeWindowsDomain',   //only need to specify this if Winuser=true
                VaultGUID : '{'+ MFVault +'}',
                //Expiration : expirationDate.format('MM/dd/yyyy')  <-- I haven't had much luck with this yet
        ]
])
def token = MFiles.getAuthorizationToken()
def resp = MFiles.setAuthorizationToken(token.data.Value.toString())
def NameOrTitleProp = MFiles.createPropertyValue (0, MFDataType.Text, 'SomeNewTitle' )
def ClassProp = MFiles.createPropertyValue(100, MFDataType.Lookup, 1)
def fileToUpload = new File('C://testFile.txt')
def resp2 = MFiles.uploadNewFile(fileToUpload,'testFile', 'txt', '0', [PropertyValues : [NameOrTitleProp,ClassProp]])


