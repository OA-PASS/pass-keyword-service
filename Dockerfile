FROM tomcat:9.0

ENV HOSTURL=pass.local
ENV CONTEXTPATH=/fcrepo/rest/submissions
ENV MAXKEYWORDS=10

ADD  target/pass-keyword-service.war /usr/local/tomcat/webapps

CMD ["catalina.sh", "run"]
