java -XX:+PrintGCDetails -Xloggc:gc.log -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=6666,suspend=n -cp ./lib/*; -Dcontainer.runtime=prod sample.baseio.http11.startup.TestHttpStartupDebug
