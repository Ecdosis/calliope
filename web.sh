#!/bin/bash
read -p "User-name (admin): " USERNAME
USERNAME=${USERNAME:-admin}
read -p "Password: (jabberw0cky)" PASSWORD
PASSWORD=${PASSWORD:-jabberw0cky}
read -p "Repository - COUCH or MONGO: (MONGO)" REPO
REPO=${REPO:-MONGO}
read -p "Web root: (/Library/WebServer/Documents)" WEBROOT
WEBROOT=${WEBROOT:-/Library/WebServer/Documents}
if [ $REPO==COUCH ]; then
  DBPORT=5984
elif [ $REPO==MONGO ]; then
  DBPORT=27017
fi
cat <<ENDOFFILE
<?xml version="1.0" encoding="ISO-8859-1"?>
<web-app xmlns="http://java.sun.com/xml/ns/javaee"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://java.sun.com/xml/ns/javaee
                      http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
  version="3.0"
  metadata-complete="true">

    <description>
     Calliope Web App.
    </description>
    <display-name>Calliope Web Service</display-name>

    <servlet>
      <servlet-name>AeseWebApp</servlet-name>
      <servlet-class>calliope.AeseWebApp</servlet-class>
      <init-param>
        <param-name>user</param-name>
        <param-value>$USERNAME</param-value>
      </init-param>
      <init-param>
        <param-name>password</param-name>
        <param-value>$PASSWORD</param-value>
      </init-param>
      <init-param>
        <param-name>repository</param-name>
        <param-value>$REPO</param-value>
      </init-param>
      <init-param>
        <param-name>dbport</param-name>
        <param-value>$DBPORT</param-value>
      </init-param>
      <init-param>
        <param-name>webroot</param-name>
        <param-value>$WEBROOT</param-value>
      </init-param>
    </servlet>

    <servlet-mapping>
        <servlet-name>AeseWebApp</servlet-name>
        <url-pattern>/*</url-pattern>
    </servlet-mapping>
</web-app>
ENDOFFILE
