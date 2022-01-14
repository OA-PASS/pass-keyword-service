FROM tomcat:9.0

ADD  target/pass-keyword-service.war /usr/local/tomcat/webapps

EXPOSE 8080

CMD ["catalina.sh", "run"]
