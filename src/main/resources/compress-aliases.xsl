<?xml version="1.0"?>
<!--
 * SPDX-FileCopyrightText: Copyright (c) 2022 Olesia Subbotina
 * SPDX-License-Identifier: MIT
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" id="compress-aliases" version="2.0">
  <!--
  Here we go through all objects that DON'T have @ref attributes
  and try to find their references in aliases. If we find them,
  we change their @base attributes. If not, we decide that they
  are in org.eolang package and also change the @base attribute.

  If some alias is badly formatted, a runtime error is issued.
  -->
  <xsl:output encoding="UTF-8"/>
  <xsl:template match="o[not(@ref) and @base and not(starts-with(@base, '.'))]">
    <xsl:variable name="o" select="."/>
    <xsl:copy>
      <xsl:attribute name="base">
        <xsl:variable name="meta" select="/program/metas/meta[contains(string($o/@base), string(part[1][text()]))]"/>
        <xsl:choose>
          <xsl:when test="$meta">
            <xsl:variable name="tail" select="$meta/part[1]"/>
            <xsl:value-of select="$tail"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$o/@base"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:apply-templates select="node()|@* except @base"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
