package com.netopstec.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class PinYinUtil {
	/**
	 * 将字符串中的中文转化为拼音,其他字符不变 适用于 品牌的拼音 添加特殊字的多音字处理
	 */
	public static String getPingYin(String inputString) {
		HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
		format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		format.setVCharType(HanyuPinyinVCharType.WITH_V);

		char[] input = inputString.trim().toCharArray();
		String output = "";

		try {
			for (int i = 0; i < input.length; i++) {
				if (Character.toString(input[i]).matches("[\\u4E00-\\u9FA5]+")) {
					String[] temp = PinyinHelper.toHanyuPinyinStringArray(input[i], format);
					if (input[i] == '长') {
						output += "chang";
						continue;
					}
					if (input[i] == '蔚') {
						output += "wei";
						continue;
					}
					if (input[i] == '都') {
						output += "du";
						continue;
					}
					output += temp[0];
				} else
					output += Character.toString(input[i]);
			}
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			e.printStackTrace();
		}
		return output;
	}

	/**
	 * 获取汉字串拼音首字母，英文字符不变
	 * 
	 * @param chinese
	 *            汉字串
	 * @return 汉语拼音首字母
	 */
	public static String getFirstSpell(String chinese) {
		StringBuffer pybf = new StringBuffer();
		char[] arr = chinese.toCharArray();
		HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
		defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] > 128) {
				try {
					String[] temp = PinyinHelper.toHanyuPinyinStringArray(arr[i], defaultFormat);
					if (temp != null) {
						if (arr[i] == '长') {
							temp[0] = "chang";
						}
						if (arr[i] == '蔚') {
							temp[0] = "wei";
						}
						if (arr[i] == '都') {
							temp[0] = "du";
						}
						pybf.append(temp[0].charAt(0));
					}
				} catch (BadHanyuPinyinOutputFormatCombination e) {
					e.printStackTrace();
				}
			} else {
				pybf.append(arr[i]);
			}
		}
		return pybf.toString().replaceAll("\\W", "").trim();
	}

	/**
	 * 获取汉字串拼音，英文字符不变
	 */
	public static String getFullSpell(String chinese) {
		if (chinese == null || "".equals(chinese)) {
			return chinese;
		}
		StringBuffer pybf = new StringBuffer();
		chinese = StringUtil.filterSpecialSymbos(chinese);
		char[] arr = chinese.toCharArray();
		HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
		defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] > 128) {
				try {
					pybf.append(PinyinHelper.toHanyuPinyinStringArray(arr[i], defaultFormat)[0]);
				} catch (BadHanyuPinyinOutputFormatCombination e) {
					e.printStackTrace();
				}
			} else {
				pybf.append(arr[i]);
			}
		}
		return pybf.toString();
	}
	
	/**
     * 将字符串中的中文字符全部替换为英文字符
     * @param chinese
     * return english
     */
    public static String ChineseCharReplaceAllEnglishChar(String chinese) {
    	String[] regs = { "！", "，", "。", "；", "（", "）", "!", ",", ".", ";", "(", ")" };
    	for ( int i = 0; i < regs.length / 2; i++ ) {
    		chinese = chinese.replaceAll (regs[i], regs[i + regs.length / 2]);
    	}
    	return chinese;
    }

	public static void main(String[] args) {
		String cnStr = "都\"市，骏,马SUV-->";
		System.out.println("aa=" + getFullSpell(cnStr));
		System.out.println("都市骏马SUV-->" + getPingYin(cnStr));
		String s = getFirstSpell("都市骏马SUV");
		System.out.println("都市骏马SUV-->" + s);
		StringBuffer sb = new StringBuffer(s);
		if (sb.length() > 1) {
			String ss = sb.delete(1, sb.length()).toString();
			System.out.println("都市骏马SUV-->" + Character.toUpperCase(ss.toCharArray()[0]) + "");
		}
	}
}
