# camm
homecamera

keytool -genkeypair -alias camm -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore src/main/resources/keystore/camm.p12 -validity 8000 -storepass cammpass -dname "CN=camm, OU=, O=, L=, S=, C=" 
