@cd %~dp0

@set JAVA_OPTS=-Djavax.net.ssl.trustStore=$JAVA_HOME/lib/security/cacerts -Dlogback.configurationFile=logback.xml -Djavafx.userAgentStylesheetUrl=modena


@call sbt run

