package com.plasmatherm.MFilesREST

import org.apache.log4j.Logger
import groovyx.net.http.RESTClient
import static groovyx.net.http.ContentType.JSON



class MFilesREST {
    def MFBaseURL
    def MFLogin
    def MFRestClient
    def authToken
    private final Logger logger = Logger.getLogger(MFilesREST.class)

    // getAuthorizationToken() - Creates a new RESTClient and gets an authentication token from the MFiles server.
    // Returns an HttpResponseDecorator - will be null if an error occurs.
    def getAuthorizationToken( ) {
        def resp
        try {
            MFRestClient = new RESTClient( MFBaseURL )
            resp = MFRestClient.post(
                    path : 'server/authenticationtokens',
                    body : [ Username : MFLogin.Username,
                            Password : MFLogin.Password,
                            WindowsUser: MFLogin.Winuser,
                            Domain : MFLogin.Domain,
                            Expiration : MFLogin.Expiration,
                            VaultGuid : MFLogin.VaultGUID
                    ],
                    requestContentType : JSON
            )
            assert resp.status == 200
            assert resp.contentType == JSON.toString()
            authToken = resp.data.Value
            logger.debug 'Authtoken: ' + authToken
            return resp
        }
        catch (e){
            logger.warn 'getAuthroizationToken: ' + e.toString()
            return resp
        }
    }

    //Used to set the authorization token rather than generate a new one.
    def setAuthorizationToken( theToken ) {
        try {
            MFRestClient = new RESTClient( MFBaseURL )
            authToken = theToken
            logger.debug 'Authorization token set'
            return sendGetRequest('server/status')
        }
        catch (e){
            logger.warn 'setAuthorizationToken: ' + e.toString()
        }
    }

    //sendGetRequest does exactly that.  Used primarily as the basis for object searching requests
    def sendGetRequest (pathString) {
        if (authToken!=null){
            def resp
            try {
                resp = MFRestClient.get(
                        path : pathString,
                        headers : ['X-Authentication' : authToken],
                        requestContentType : JSON
                )
                assert resp.status == 200
                assert resp.contentType == JSON.toString()
                logger.debug 'sendGetRequest - PathString: ' + pathString
                return resp.data
            }
            catch (e){
                logger.warn 'sendGetRequest: ' + e.toString()
                return resp
            }
        }
    }

    //sendGetRequest where the request requires a query string
    def sendGetRequest (pathString, theQuery) {
        if (authToken!=null) {
            def resp
            try {
                resp = MFRestClient.get(
                        path : pathString,
                        headers : ['X-Authentication' : authToken],
                        queryString: theQuery,
                        requestContentType : JSON
                )
                assert resp.status == 200
                assert resp.contentType == JSON.toString()
                logger.debug 'sendGetRequest - PathString: ' + pathString + ', theQuery: ' + theQuery
                return resp.data
            }
            catch (e){
                logger.warn 'sendGetRequest: ' + e.toString()
                return resp
            }
        }
    }

    //Sends a get request where the content type is not JSON
    def sendGetRequest (pathString, theQuery, contentType) {
        if (authToken!=null) {
            def resp
            try {
                resp = MFRestClient.get(
                        path : pathString,
                        headers : ['X-Authentication' : authToken],
                        requestContentType : contentType
                )
                assert resp.status == 200
                logger.debug 'sendGetRequest - PathString: ' + pathString + ', theQuery: ' + theQuery + ', contentType: ' + contentType
                return resp.data
            }
            catch (e){
                logger.warn 'sendGetRequest' + e.toString()
                return resp
            }
        }
    }

    //sendPostRequest is mirror image to sendGetRequest.  Used primarily as basis for object creation/modification requests.
    def sendPostRequest (pathString, theBody) {
        if (authToken!=null) {
            def resp
            try {
                resp = MFRestClient.post(
                        path : pathString,
                        headers : ['X-Authentication' : authToken],
                        body: theBody,
                        requestContentType : JSON
                )
                assert resp.status == 200
                assert resp.contentType == JSON.toString()
                logger.debug 'sendPostRequest: ' + pathString + ', theBody: ' + theBody
                return resp.data
            }
            catch (e){
                logger.warn 'sendPostRequest: ' + e.toString()
                return resp
            }
        }
    }

    //send a post request with an alternate content type
    def sendPostRequest (pathString, theBody, reqContentType) {
        if (authToken!=null) {
            def resp
            try {
                resp = MFRestClient.post(
                        path : pathString,
                        headers : ['X-Authentication' : authToken],
                        body: theBody,
                        requestContentType : reqContentType
                )
                assert resp.status == 200
                assert resp.contentType == JSON.toString()
                logger.debug 'sendPostRequest: ' + pathString + ', theBody: ' + theBody + ', reqContentType: ' + reqContentType
                return resp.data
            }
            catch (e){
                logger.warn 'sendPostRequest: ' + e.toString()
                return resp
            }
        }
    }


    //getRootViews() will return a collection of the root views
    def getRootViews( ) {
        return sendGetRequest('views/items')
    }

    //getRecentlyAccessedByMe will return a collection of objects recently accessed by me.
    def getRecentlyAccessedByMe( ) {
        return sendGetRequest('recentlyaccessedbyme')
    }

    //getObjects will return a collection of objects of the type specified by MFObjectTypeID.
    def getObjects( MFObjectTypeID ) {
        return sendGetRequest('objects/' + MFObjectTypeID)
    }

    //getObjects will return the latest version of the object specified by the MFObjectTypeID and MFObjectID.
    def getObjects( MFObjectTypeID, MFObjectID ) {
        return sendGetRequest('objects/' + MFObjectTypeID + '/' + MFObjectID)
    }

    //getObjects will return a specific object version of the type specified by MFObjectTypeID.
    def getObjects( MFObjectTypeID, MFObjectID, MFObjectVersion ) {
        return sendGetRequest('objects/' + MFObjectTypeID + '/' + MFObjectID + '/' + MFObjectVersion)
    }

    //getObjectProperties will return a collection of the properties of an object.
    def getObjectProperties( MFObjVer ) {
        return sendGetRequest('objects/' + MFObjVer.type + '/' + MFObjVer.ID + '/' + MFObjVer.Version +'/properties')
    }

    //getObjectProperties will return a specific property of an object.
    def getObjectProperties( MFObj, MFPropertyID ) {
        return sendGetRequest('objects/' + MFObj.ObjVer.Type + '/' + MFObj.ObjVer.ID + '/' + MFObj.ObjVer.Version +'/properties/' + MFPropertyID)
    }

    //searchForObjects will return the results of a query
    def searchForObjects( theQuery ) {
        return sendGetRequest('objects.aspx', theQuery)
    }

    //createNewObject generates a new M-Files object with the included properties
    def createNewObject( MFObjectTypeID, MFObjectProperties ) {
        return sendPostRequest('objects/' + MFObjectTypeID + '.aspx', MFObjectProperties)
    }

    //uploadNewFile takes a file object, loads it into M-Files, and associates it with the correct document/properties.
    def uploadNewFile (File fileToUpload, String fileName, String fileExtension, String MFObjectTypeID, Map MFObjectProperties) {
        def uploadInfo
        def fileInfo
        def resp
        try {
            //upload file
            def bifs = new BufferedInputStream(new FileInputStream(fileToUpload))
            def bofs = new ByteArrayOutputStream()
            bofs << bifs
            bifs.close()
            bofs.close()
            uploadInfo = sendPostRequest( 'files', bofs, 'application/octet-stream' )
            logger.debug uploadInfo.toString()

            //set up properties
            fileInfo = [Files : [uploadInfo + [Title : fileName, Extension : fileExtension]]]
            MFObjectProperties = MFObjectProperties + fileInfo
            logger.debug MFObjectProperties.toString()

            //check if a title was provided - if not, use file name
            if ( !getPropertyValue(MFObjectProperties,0) ) {
                MFObjectProperties.PropertyValues = MFObjectProperties.PropertyValues + createPropertyValue (0, MFDataType.Text, fileName)
            }
            logger.debug 'uploadNewFile: ' + MFObjectProperties
            //Create document from file
            resp = createNewObject( MFObjectTypeID, MFObjectProperties )
            return resp
        }
        catch (e) {
            logger.warn 'uploadNewFile: ' + e.toString()
            return resp
        }
    }



    //takes a stream of a file and pushes it as a temporary upload to M-Files
    def uploadStream (ByteArrayInputStream bifs, MFObjectTypeID, MFObjectProperties) {
        def uploadInfo
        def resp
        try {
            def bofs = new ByteArrayOutputStream()
            bofs << bifs
            //upload file
            uploadInfo = sendPostRequest( 'files', bofs, 'application/octet-stream' )
            bifs.close()
            bofs.close()
            //create object with properties
            resp = createNewObject( MFObjectTypeID, MFObjectProperties )
            assert resp.status == 200
            assert resp.contentType == JSON.toString()
            return resp
        }
        catch (e) {
            logger.warn 'uploadStream: ' + e.toString()
            return resp
        }
    }

    //gets a stream of a file from M-Files.
    def downloadFileToStream ( MFObjectType, MFObjectID, MFFileID ){
        ByteArrayInputStream respStream
        def resp
        def MFVersion = 'latest'
        def pathString = 'objects/'+ MFObjectType + '/' + MFObjectID + '/' + MFVersion + '/files/' + MFFileID + '/content'
        try {
            resp = sendGetRequest (pathString,'', 'application/octet-stream')
            respStream << resp.data
            respStream.close()
            return respStream
        }
        catch (e) {
            logger.warn 'downloadFileToStream: ' + e.toString()
            return resp
        }
    }

    //createPropertyValue creates a property value object of the specified property, property type, and value
    //It is intended to be used to define metadata when creating objects through REST.
    def createPropertyValue (propertyDef, typeDef, value ) {
        switch (typeDef) {
            case [MFDataType.Lookup] :
                return [PropertyDef : propertyDef, TypedValue : [DataType : typeDef, Lookup : [Item : value]]]
            case [MFDataType.MultiSelectLookup] :
                def lookups = []
                value.each { lookup ->
                    lookups = lookups.plus([Item : lookup])
                }
                return [PropertyDef : propertyDef, TypedValue : [DataType : typeDef, Lookups : lookups]]
            default :
                return [PropertyDef : propertyDef, TypedValue : [DataType : typeDef, Value : value]]
        }
    }

    //getPropertyValue searches a list of property values for the one that matches a specific property definition.
    //If found, the PropertyValue is returned, if not - null
    def getPropertyValue (propertyValues, propertyDef) {
        def resp
        propertyValues.PropertyValues.each {pval ->
            if (pval?.PropertyDef == propertyDef) {
                resp = pval
            }
        }
        return resp
    }
}
