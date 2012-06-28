<%@ page contentType="application/xrds+xml"%><?xml version="1.0" encoding="UTF-8"?>
<xrds:XRDS
  xmlns:xrds="xri://$xrds"
  xmlns:openid="http://openid.net/xmlns/1.0"
  xmlns="xri://$xrd*($v*2.0)">
  <XRD>
   <!-- Change the URI for OpenID2 pointing to where the provider is located -->
    <Service priority="0">
      <Type>http://specs.openid.net/auth/2.0/signon</Type>
      <URI>https://localhost:8443/OpenIdProvider/openid/provider/</URI>
    </Service>
  </XRD>
</xrds:XRDS>
