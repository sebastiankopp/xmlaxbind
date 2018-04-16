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

import static java.lang.reflect.Modifier.isTransient;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.sebastiankopp.xmlaxbind.typemapping.DefaultTypeMapper;

public class ClassModel {

	private final ClassModel superclassMdl;
	private final Map<String,ClassModel> cTypeMappings = new HashMap<>();
	private final Map<String,ClassModel> collectionCtypeMappings = new HashMap<>();
	
	private final Map<String,TypeMapper<?>> sTypeMappings = new HashMap<>();
	private final Map<String,TypeMapper<?>> collectionStypeMappings = new HashMap<>();
	
	private final Map<String,Field> fieldMappings = new HashMap<>();
	
	ClassModel(Class<?> boundClass, ClassModelResolver resolver) {
		final Class<?> superclass = boundClass.getSuperclass();
		superclassMdl = (superclass == Object.class || boundClass.getDeclaredAnnotation(ExcludeSuperclass.class) != null)
				? null : resolver.resolveClassModel(superclass);
		resolver.registerModel(this);
		stream(boundClass.getDeclaredFields())
				.filter(e -> !isTransient(e.getModifiers()))
				.forEachOrdered(e -> processField(e, resolver));
	}

	private void processField(Field field, ClassModelResolver resolver) {
		try {
			List<String> filteringNames = getFilteringNames(field);
			filteringNames.forEach(fn -> fieldMappings.put(fn, field));
			Optional<TypeMapper<?>> sTypeMapper = tryResolveTypeMapper(field);
			final Class<?> fieldType = field.getType();
			if (sTypeMapper.isPresent()) {
				if (fieldType == List.class) {
					filteringNames.forEach(fn -> collectionStypeMappings.put(fn, sTypeMapper.get()));
				} else {
					filteringNames.forEach(fn -> sTypeMappings.put(fn, sTypeMapper.get()));
				}
			} else {
				if (fieldType == List.class) {
					filteringNames.forEach(fn -> collectionCtypeMappings.put(fn, resolveListClassModel(field, resolver)));
				} else {
					filteringNames.forEach(fn -> cTypeMappings.put(fn, resolver.resolveClassModel(fieldType)));
				}
			}
			
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}
	
	private Optional<TypeMapper<?>> tryResolveTypeMapper(Field f) throws InstantiationException, IllegalAccessException {
		final CustomMapping mappingAnnotation = f.getAnnotation(CustomMapping.class);
		if (mappingAnnotation != null && mappingAnnotation.mappedBy() != DefaultTypeMapper.class) {
			return Optional.of(mappingAnnotation.mappedBy().newInstance());
		}
		return Optional.empty();
	}
	
	private ClassModel resolveListClassModel(Field f, ClassModelResolver resolver) {
		if (List.class != f.getType()) {
			throw new IllegalStateException("The given field should be of the type java.util.List bus was " + f.getType());
		}
		Type typeArg = ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0];
		if (!(typeArg instanceof Class)) {
			throw new IllegalStateException("The type " + typeArg + " may not be used as a type parameter.");
		}
		return resolver.resolveClassModel((Class<?>) typeArg);
	}

	private List<String> getFilteringNames(Field f) {
		CustomMapping mappingAnnotation = f.getAnnotation(CustomMapping.class);
		return (mappingAnnotation != null && mappingAnnotation.names().length > 0) ?
				asList(mappingAnnotation.names()) : asList(f.getName());
	}
	
		
}
