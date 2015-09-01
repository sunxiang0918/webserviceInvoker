package cn.sonshou.wsinvoker.lang2.parse.operation.util;

import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLParam;

public class OrderParamUtil
{
	public static void orderParams(JWSDLParam[] param, String[] orderList)
	{
		if(param!=null&&orderList!=null&&orderList.length>0){
			JWSDLParam[] temp = new JWSDLParam[param.length];
			for(int i=0; i<orderList.length; i++)
			{
				for(int k=0; k<param.length; k++)
				{
					if(param[k].getParamName()!=null
					&&param[k].getParamName().getLocalPart().equalsIgnoreCase(orderList[i])){
						temp[i] = param[k];
					}
				}
			}
			
			for(int i=0; i<temp.length; i++)
			{
				param[i] = temp[i];
			}
		}
	}
}
