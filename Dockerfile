FROM tomcat:9.0

ADD  target/pass-keyword-service.war /usr/local/tomcat/webapps

CMD ["catalina.sh", "run"]
