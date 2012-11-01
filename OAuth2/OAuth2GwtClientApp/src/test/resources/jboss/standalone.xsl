<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                exclude-result-prefixes="xs xsl xsi">

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="*[@default-virtual-server]">
        <xsl:copy>
            <connector name="http" protocol="HTTP/1.1" scheme="http" socket-binding="http" redirect-port="8443"/>
            <connector name="https" protocol="HTTP/1.1" scheme="https" socket-binding="https" secure="true">
                <ssl name="https" password="${keystore.password}"
                     certificate-key-file="${jboss.config.dir}/server.keystore" protocol="TLS"/>
            </connector>
            <virtual-server name="default-host" enable-welcome-root="true">
                <alias name="localhost"/>
                <alias name="example.com"/>
            </virtual-server>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="*[@port='8080']">
        <xsl:copy>
            <xsl:attribute name="name">
                <xsl:value-of select="'http'"/>
            </xsl:attribute>
            <xsl:attribute name="port">
                <xsl:value-of select="'18081'"/>
            </xsl:attribute>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="*[@path='deployments']">
        <xsl:copy>
            <xsl:attribute name="path">
                <xsl:value-of select="'deployments'"/>
            </xsl:attribute>
            <xsl:attribute name="relative-to">
                <xsl:value-of select="'jboss.server.base.dir'"/>
            </xsl:attribute>
            <xsl:attribute name="scan-interval">
                <xsl:value-of select="'5000'"/>
            </xsl:attribute>
            <xsl:attribute name="deployment-timeout">
                <xsl:value-of select="'1200'"/>
            </xsl:attribute>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="node()[name(.)='extensions']">
        <xsl:copy-of select="."/>
        <system-properties>
            <!--<property name="javax.net.debug" value="ssl"/>-->
            <property name="javax.net.ssl.trustStore" value="${jboss.config.dir}/client.truststore"/>
            <property name="javax.net.ssl.trustStorePassword" value="${truststore.password}"/>
            <property name="javax.net.ssl.trustStoreType" value="jks"/>
        </system-properties>
    </xsl:template>

    <!--<xsl:template match="*[@name='INFO']">-->
    <!--<xsl:copy>-->
    <!--<xsl:attribute name="name">-->
    <!--<xsl:value-of select="'DEBUG'"/>-->
    <!--</xsl:attribute>-->
    <!--</xsl:copy>-->
    <!--</xsl:template>-->

</xsl:stylesheet>