/*
 * Copyright (C) Sebastian Kopp, 2018
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package de.sebastiankopp.xmlaxbind;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class DelegatingXmlParseHandler extends DefaultHandler {
	private final Object objToBuild;

	private DelegatingXmlParseHandler delegate = null;
	private boolean inSimpleTextNode = false;
	private final DelegatingXmlParseHandler parent;
	private final ClassModel model;
	private final ClassModelResolver resolver;
	public DelegatingXmlParseHandler(Class<?> target, ClassModelResolver res) {
		this(target, null, res);
	}
	
	private DelegatingXmlParseHandler(Class<?> target, DelegatingXmlParseHandler parent, ClassModelResolver res) {
		try {
			this.parent = parent;
			objToBuild = target.newInstance();
			resolver = res;
			model = new ClassModel(target, resolver);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException("The class " + target + " could not be properly instantiated." 
					+ " Please ensure that it has a public default constructor.", e);
		}
	}
	
	public Object getParsedModel() {
		return objToBuild;
	}
	
	private void pushBack() {
		
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		resolveElementName(qName, localName);
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		resolveElementName(qName, localName);
		
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		
	}
	
	private String resolveElementName(String qname, String locName) {
		if (locName != null && !locName.isEmpty()) {
			return locName;
		} else  if (!qname.contains(":")) {
			return qname;
		} else {
			return qname.split(":")[1];
		}
	}
}
