package cn.sonshou.wsinvoker.lang2.parse.vo;

import java.util.List;

/**
 * Choice类型的字段|
 * 参数类型(Class)的元素(Field)描述,类似java.lang.reflect.Field
 * name(字段名)
 * namespace(字段的前缀)
 * fieldClass(字段类型)
 * @author sonshou
 */
public class JWSDLParamTypeChoiceField extends JWSDLParamTypeField
{
	private List<JWSDLParamTypeField> choices;

    public void setChoices(List<JWSDLParamTypeField> choices) {
		this.choices = choices;
	}
	
	public List<JWSDLParamTypeField> getChoices() {
		return choices;
	}
}
