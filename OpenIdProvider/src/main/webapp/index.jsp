<%@ page contentType="application/xrds+xml"%><?xml version="1.0" encoding="UTF-8"?>
<xrds:XRDS
  xmlns:xrds="xri://$xrds"
  xmlns="xri://$xrd*($v*2.0)">
  <XRD>
    <Service priority="0">
      <Type>http://specs.openid.net/auth/2.0/server</Type>
      <Type>http://openid.net/srv/ax/1.0</Type>
      <URI>https://localhost:8443/OpenIdProvider/openid/provider</URI>
    </Service>
  </XRD>
</xrds:XRDS>
