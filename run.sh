java -Duser.language=en -Duser.country=US -cp $(for i in lib/*.jar ; do echo -n $i: ; done).:./orabbix_1.2.4.jar com.smartmarmot.orabbix.bootstrap start ./conf/config.props &
