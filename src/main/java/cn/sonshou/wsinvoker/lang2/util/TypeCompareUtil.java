package cn.sonshou.wsinvoker.lang2.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLParamType;
import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLParamTypeChoiceField;
import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLParamTypeField;
import cn.sonshou.wsinvoker.lang2.w3c.Constant;

/**
 * 处理WSDL类型是否匹配本地JAVA类型
 * @author sonshou
 */
public class TypeCompareUtil 
{
    private static Logger logger = LoggerFactory.getLogger(TypeCompareUtil.class);
         /**
          * 判断参数是否匹配,只判断类型是否匹配
          * @param jwsdlType
          * @param apiType
          * @param compareType
          * @return
          */

    public static boolean isParamTypeMatch(JWSDLParamType jwsdlType, Class apiType, String compareType)
    {

        if (apiType==null) {
            if(logger.isDebugEnabled())
                logger.error("分析参数匹配失败:传入的apiType为空");
            return false;
        }

        if(logger.isDebugEnabled()) {
            logger.debug("--a--" + "分析参数匹配: wsdlparam-" + jwsdlType + "   |and|  apiparam-" + apiType);
        }
        if(PrimitiveTypeUtil.isMapping(jwsdlType, apiType))
        {
            if(logger.isDebugEnabled()) {
                logger.debug("---" + "分析参数匹配: 都是基础类型");
                logger.debug("---" + "分析参数匹配: 匹配成功");
            }
            return true;
        }else if(jwsdlType.isArray()&&apiType.isArray()) {
            if (logger.isDebugEnabled())
                logger.debug("---" + "分析参数匹配: 都是数组类型,分析数组元素类型");
            JWSDLParamType jwsdlArrayElementType = jwsdlType.getArrayElementType();
            Class apiArrayElementType = apiType.getComponentType();
            boolean result = isParamTypeMatch(jwsdlArrayElementType, apiArrayElementType, "array");
            if (result) {
                if (logger.isDebugEnabled())
                    logger.debug("---" + "分析参数匹配: 数组匹配成功");
                return true;
            }
        }else if(!jwsdlType.isArray()&&!jwsdlType.isPrimitive()
            &&jwsdlType.getDeclaredFields()!=null&&jwsdlType.getDeclaredFields().length==1
            &&jwsdlType.getDeclaredFields()[0].getType().isArray()
            &&apiType.isArray()){
            //2015-05-05 sonshou 增加一种数组处理--即wsdl中schema定义的是一个complextype，type中是一堆
            //TODO: 是否这里也存在choice的情况？？
            JWSDLParamTypeField field = jwsdlType.getDeclaredFields()[0];
            JWSDLParamType wsdlType = field.getType();
            Class apiArrayElementType = apiType.getComponentType();
            boolean result = isParamTypeMatch(wsdlType.getArrayElementType(), apiArrayElementType, "array");
            if(result){
                if (logger.isDebugEnabled())
                    logger.debug("---" + "分析参数(包含方式)匹配: 数组匹配成功");
                //修改匹配结果
                //将子元素的数组元素类型作为自己的类型
                wsdlType.setJavaType(Object.class);
                wsdlType.setDeclaredFields(wsdlType.getArrayElementType().getDeclaredFields());
                wsdlType.setArray(false);
                wsdlType.setPrimitive(wsdlType.getArrayElementType().isPrimitive());
                wsdlType.setArrayElementType(null);
                //将当前元素设置为数组，且数组元素是子元素
                jwsdlType.setJavaType(Object[].class);
                jwsdlType.setDeclaredFields(null);
                jwsdlType.setArray(true);
                jwsdlType.setArrayElementType(wsdlType);
                jwsdlType.setPrimitive(false);
                jwsdlType.setComplexTypeArray(true);
                jwsdlType.setComplexTypeArrayFieldElementName(field.getQname());
                return true;
            }
        }else if(jwsdlType.getQname()!=null&&jwsdlType.getQname().getLocalPart().equalsIgnoreCase("anyType")){
            return true;
        }else{
            if(logger.isDebugEnabled())
                logger.debug("---" + "分析参数匹配: 都是对象类型,分析对象字段(非Wrapper的分析)");
            JWSDLParamTypeField[] jwsdlTypeFields = jwsdlType.getDeclaredFields();
            Field[] apiFields = apiType.getDeclaredFields();
            //copy一个，用来处理。
            List<Field> list = new ArrayList<Field>(Arrays.asList(apiFields));

            if((!jwsdlType.isPrimitive()&& PrimitiveTypeUtil.isPrimitive(apiType))
                ||(jwsdlType.isPrimitive()&&!PrimitiveTypeUtil.isPrimitive(apiType))){
                return false;
            }

            if(jwsdlTypeFields != null){
                if(jwsdlTypeFields.length==apiFields.length) {
                    if (logger.isDebugEnabled())
                        logger.debug("---" + "分析参数匹配: 对象字段数目匹配,分析字段(非Wrapper的分析)");
                    boolean fieldMatch = true;//最终结果
                    for (int i = 0; i < jwsdlTypeFields.length; i++) {
                        JWSDLParamType jwsdlTypeFieldType = jwsdlTypeFields[i].getType();
                        if (logger.isDebugEnabled())
                            logger.debug("****---" + "分析wsdl字段：" + jwsdlTypeFields[i].getQname());
                        if (jwsdlType.getQname() != null) {
                            /**嵌套类型*****/
                            if ((jwsdlTypeFieldType.isArray()
                                && jwsdlTypeFieldType.getArrayElementType().getQname() != null
                                && jwsdlTypeFieldType.getArrayElementType().getQname().equals(jwsdlType.getQname()))
                                ||
                                (jwsdlTypeFieldType.getQname().equals(jwsdlType.getQname()))) {
                                if (logger.isDebugEnabled())
                                    logger.debug("                |_____嵌套类型" + jwsdlTypeFields[i].getQname());
                                continue;
                            }
                        }

                        boolean hasMatchedField = false;

                        for(Iterator<Field> itr = list.iterator();itr.hasNext();){
                            Field apiField = itr.next();
                            Class apiFieldType = apiField.getType();
                            //2015-04-28 sonshou 增加choice处理 @see SOAP11DocumentOperationParser 379
                            if (jwsdlTypeFieldType.getJavaType().equals(Constant.Choice.class)) {
                                JWSDLParamTypeChoiceField choiceField = (JWSDLParamTypeChoiceField) jwsdlTypeFields[i];
                                if (choiceField == null) {
                                    throw new IllegalStateException("Scchema中Choice类型没有节点数据");
                                }
                                List<JWSDLParamTypeField> choices = choiceField.getChoices();
                                if (choices == null || choices.size() < 1) {
                                    throw new IllegalStateException("Scchema中Choice类型没有节点数据");
                                }
                                for (JWSDLParamTypeField field : choices) {
                                    if (logger.isDebugEnabled())
                                        logger.debug("****---" + "分析choice字段：" + field.getQname());
                                    JWSDLParamType choiceFieldType = field.getType();
                                    hasMatchedField = isParamTypeMatch(choiceFieldType, apiFieldType, "choicefield");
                                    if(field.getQname()!=null&&!field.getQname().getLocalPart().startsWith("UnNamed")){
                                        hasMatchedField = field.getQname().getLocalPart().equalsIgnoreCase(apiFields[i].getName());
                                    }
                                    if (hasMatchedField) {
                                        //对于字段数量相同情况下的choice，只要任意一个匹配即可
                                        if (logger.isDebugEnabled())
                                            logger.debug("****---choice字段成功匹配" + field.getQname());
                                        choiceFieldType.setJavaType(apiFieldType);
                                        break;
                                    }
                                }
                            }else {
                                hasMatchedField = isParamTypeMatch(jwsdlTypeFieldType, apiFieldType, "field");
                                QName jwsdlFieldName = jwsdlTypeFields[i].getQname();
                                String apiName = apiField.getName();
                                hasMatchedField = hasMatchedField && jwsdlFieldName.getLocalPart().equalsIgnoreCase(apiName);
                                if (hasMatchedField) {
                                    if (logger.isDebugEnabled())
                                        logger.debug("****---成功匹配" + jwsdlTypeFields[i].getQname());
                                    jwsdlTypeFieldType.setJavaType(apiFieldType);
                                }
                            }

                            if(hasMatchedField){
                                itr.remove();
                                break;
                            }
                        }

                        fieldMatch = fieldMatch && hasMatchedField;
                    }
                    if (fieldMatch) {
                        if (logger.isDebugEnabled())
                            logger.debug("---" + "分析参数匹配: 对象类型成功(非Wrapper的分析)");
                        return true;
                    }
                }else if(jwsdlTypeFields.length<apiFields.length){
                    if (logger.isDebugEnabled())
                        logger.debug("===" + "分析参数匹配: 对象字段数目不等,可能包含choice字段");
                    //2015-04-28 sonshou 当wsdl参数少于params的时候，有可能采用了choice
                    boolean fieldMatch = true;//最终结果
                    boolean hasChoice = false;

                    for (int k=0; k < jwsdlTypeFields.length&&list.size()>0;k++) {
                        if (logger.isDebugEnabled())
                            logger.debug("****---【choice条件下】分析wsdl字段：" + jwsdlTypeFields[k].getQname());

                        if(jwsdlTypeFields[k].getType().getJavaType().equals(Constant.Choice.class)){
                            //java对象为空或当前不匹配，继续寻找下一个java对象，但是不能超出k的剩余范围
                            hasChoice = true;
                            //choice字段的匹配
                            JWSDLParamTypeChoiceField choiceField = (JWSDLParamTypeChoiceField) jwsdlTypeFields[k];
                            if (choiceField == null) {
                                throw new IllegalStateException("Scchema中Choice类型没有节点数据");
                            }
                            List<JWSDLParamTypeField> choices = choiceField.getChoices();
                            if (choices == null || choices.size() < 1) {
                                throw new IllegalStateException("Scchema中Choice类型没有节点数据");
                            }

                                /*------对choice字段进行循环查找，以全部匹配对象中的顺序字段-----*/
                            int size = choices.size(); //size个匹配
                            int matchcount = 0;
                            for (JWSDLParamTypeField field : choices) {
                                JWSDLParamType choiceFieldType = field.getType();
                                if (logger.isDebugEnabled())
                                    logger.debug("****---分析choice字段：" + field.getQname());

                                    /*对于字段数量不同的choice，需要把所有choice找到
                                     */
                                for (Iterator<Field> itr = list.iterator(); itr.hasNext(); ) {
                                    Field apiField = itr.next();

                                    Class apiFieldType = apiField.getType();
                                    boolean choiceFieldMatch;
                                    try {
                                        choiceFieldMatch = isParamTypeMatch(choiceFieldType, apiFieldType, "choice");
                                    } catch (IllegalArgumentException e) {
                                        logger.warn(e.toString());
                                        continue;
                                    }
                                    if (field.getQname() != null && !field.getQname().getLocalPart().startsWith("UnNamed")) {
                                        choiceFieldMatch = field.getQname().getLocalPart().equalsIgnoreCase(apiField.getName());
                                    }

                                    if (logger.isDebugEnabled())
                                        logger.debug("****--" + (choiceFieldMatch ? "s" : "n") + "--choice选项匹配" + (choiceFieldMatch ? "成功," : "失败,") + field.getQname());

                                    if (choiceFieldMatch) {
                                        choiceFieldType.setJavaType(apiFieldType);
                                        matchcount++;
                                        itr.remove();
                                        break;
                                    }
                                }
                            }

                            fieldMatch = fieldMatch && matchcount>0;

                            if(!fieldMatch){
                                if (logger.isDebugEnabled())
                                    logger.debug("--n--" + "【choice条件下】分析参数匹配: 对象类型失败(非Wrapper的分析)");
                                return false;
                            }
                        }else{
                            boolean hasMatch = false;
                            for(Iterator<Field> itr = list.iterator();itr.hasNext();){
                                Field apiField = itr.next();
                                boolean hasmatch = isParamTypeMatch(jwsdlTypeFields[k].getType(), apiField.getType(), "choice");
                                hasmatch = hasmatch && jwsdlTypeFields[k].getQname().getLocalPart().equalsIgnoreCase(apiField.getName());
                                if(hasmatch) {
                                    jwsdlTypeFields[k].getType().setJavaType(apiField.getType());
                                    if (logger.isDebugEnabled())
                                        logger.debug("****---成功匹配" + jwsdlTypeFields[k].getQname());
                                    itr.remove();
                                    hasMatch = true;
                                    break;
                                }
                            }

                            fieldMatch = fieldMatch&&hasMatch;

                            if(!fieldMatch){
                                if (logger.isDebugEnabled())
                                    logger.debug("--n--" + "【choice条件下】分析参数匹配: 对象类型失败(非Wrapper的分析)");
                                return false;
                            }

                        }
                    }

                    if(!hasChoice){
                        throw new IllegalArgumentException("JAVA对象字段大于Schema定义字段，但是Schema中没有Choice类型定义");
                    }

                    if (fieldMatch) {
                        if (logger.isDebugEnabled())
                            logger.debug("--s--" + "【choice条件下】分析参数匹配: 对象类型成功(非Wrapper的分析)");
                        return true;
                    } else{
                        if (logger.isDebugEnabled())
                            logger.debug("--n--" + "【choice条件下】分析参数匹配: 对象类型失败(非Wrapper的分析)");
                        return false;
                    }
                }
            }
        }

        JWSDLParamTypeField[] jwsdlTypeFields = jwsdlType.getDeclaredFields();
        List<String> list1 = new ArrayList<>();
        for(int i=0; jwsdlTypeFields!=null&&i<jwsdlTypeFields.length; i++){
            list1.add("<"+jwsdlTypeFields[i].getType()+">");
        }

        Field[] apiFields = apiType.getDeclaredFields();
        List<String> list2 = new ArrayList<>();
        for(int i=0; apiFields!=null&&i<apiFields.length; i++){
            list2.add("<"+apiFields[i].getType()+">");
        }
        if(compareType.equalsIgnoreCase("base"))
            if(logger.isDebugEnabled())
                logger.debug("--n--" + "分析参数匹配(非Wrapper): 参数不匹配  wsdlParam对象-拥有字段<" + list1 + ">\n                                    javaparam对象-拥有字段" + apiType + "<" + list2 + ">");
        return false;
    }

}
