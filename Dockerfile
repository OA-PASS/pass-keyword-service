FROM tomcat

ENV IMAGE_JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/jre

EXPOSE ${pass.keyword.service.port}

ADD  target/pass-keyword-service.war /usr/local/tomcat/webapps/
RUN  echo "networkaddress.cache.ttl=10" >> ${IMAGE_JAVA_HOME}/lib/security/java.security

CMD ["catalina.sh", "run"]
