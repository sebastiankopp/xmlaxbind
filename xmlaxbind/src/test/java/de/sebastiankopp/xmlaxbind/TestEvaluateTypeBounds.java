package de.sebastiankopp.xmlaxbind;

import static java.util.Arrays.stream;
import static org.testng.Assert.assertSame;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

public class TestEvaluateTypeBounds {

	@Test
	public void test1() {
		Type firstTypeParam = stream(new StringMapper().getClass().getGenericInterfaces())
				.map(ParameterizedType.class::cast)
				.map(e -> e.getActualTypeArguments()[0])
				.findFirst().orElseThrow(AssertionError::new);
		assertSame(firstTypeParam, String.class);
	}
	
	@Test
	public void test2() throws Exception {
		Field field = SomeFoo.class.getDeclaredField("blaah");
		final Type[] typeArgs = ((ParameterizedType)field.getGenericType()).getActualTypeArguments();
		Type typeArg = typeArgs[0];
		Type typeArg2 = typeArgs[1];
		System.out.println((Class<?>) typeArg);
		System.out.println(typeArg2);
		
	}
	
	class StringMapper implements TypeMapper<String> {

		@Override
		public String convertValue(String rawValue) {
			return rawValue;
		}
		
	}
	
	class SomeFoo {
		private final Map<String,List<String>> blaah = new HashMap<>();

		public List<String> getBlaah() {
			return new ArrayList<>(blaah.keySet());
		}
	}
}