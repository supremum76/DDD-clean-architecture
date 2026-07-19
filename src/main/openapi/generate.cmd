cd .\out
java -jar ..\openapi-generator-cli.jar ^
generate -i openapi.yml ^
-g spring ^
-o openapi ^
--global-property apis ^
--global-property models ^
--global-property supportingFiles ^
--package-name microarch.delivery.adapters.in.http.openapi ^
--skip-validate-spec ^
--additional-properties=interfaceOnly=false,skipDefaultInterface=true,useTags=true,use.JakartaEe=true,modelPackage=model,apiPackage=api,delegatePattern=false;