# to run the server

    sbt test run

To try with a curl command you may execute

     curl -F "file=@~/src/test/resources/lorem.txt" localhost:8080/

The server run in port 8080 and accepts multi-part https://www.w3.org/TR/html401/interact/forms.html#h-17.13.4.2
The file fieldname for the multipart must be `file`

