@rem windows

@set JAVA_OPTS=-Djavax.net.ssl.trustStore=$JAVA_HOME/lib/security/cacerts -Dlogback.configurationFile=logback.xml -Djavafx.userAgentStylesheetUrl=modena

@java -jar ./pricecompare.jar

