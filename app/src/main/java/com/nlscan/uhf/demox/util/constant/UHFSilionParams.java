package com.nlscan.uhf.demox.util.constant;

import com.nlscan.uhf.lib.UHFReader;

/**
 * Settings parameters constants
 */
public class UHFSilionParams {

	public static final int UHF_STATE_CONNECTED = 1;
	public static final int UHF_STATE_DISCONNECTED = 2;
	public static final int UHF_STATE_CONNECTING = 3;
	public static final int UHF_STATE_START_INVENTORY = 4;
	public static final int UHF_STATE_STOP_INVENTORY = 5;
	public static final int UHF_STATE_CONNECT_FAIL = 0x06;

	/**
	 * Gen2 Protocal
	 */
	public final static class POTL_GEN2_SESSION{
		public final static String KEY = "POTL_GEN2_SESSION";
		/**Gen2*/
		public final static String PARAM_POTL_GEN2_SESSION = "PARAM_POTL_GEN2_SESSION";
	}
	
	/**
	 * Gen2 Q
	 */
	public final static class POTL_GEN2_Q{
		public final static String KEY = "POTL_GEN2_Q";
		/**Gen2 Q*/
		public final static String PARAM_POTL_GEN2_Q = "PARAM_POTL_GEN2_Q";
	}
	
	/**
	 * Gen2 base encode type
	 */
	public final static class POTL_GEN2_TAGENCODING{
		public final static String KEY = "POTL_GEN2_TAGENCODING";
		/**Gen2 base encode type*/
		public final static String PARAM_POTL_GEN2_TAGENCODING = "PARAM_POTL_GEN2_TAGENCODING";
	}

	/**
	 * Max len of EPC，by bit
	 */
	public final static class POTL_GEN2_MAXEPCLEN{
		public final static String KEY = "POTL_GEN2_MAXEPCLEN";
		public final static String PARAM_POTL_GEN2_MAXEPCLEN = "PARAM_POTL_GEN2_MAXEPCLEN";
	}
	
	/**
	 * 读写器发射功率
	 */
	public final static class RF_ANTPOWER{
		public final static String KEY = "RF_ANTPOWER";
		/**读写器发射功率,数据格式:天线ID, 读功率,写功率|天线ID, 读功率,写功率*/
		public final static String PARAM_RF_ANTPOWER = "PARAM_RF_ANTPOWER";
	}
	
	/**
	 * 读写器最大输出功率(单位为centi-dbm)(只读)
	 */
	public final static class RF_MAXPOWER{
		public final static String KEY = "RF_MAXPOWER";
		/**读写器最大输出功率 */
		public final static String PARAM_RF_MAXPOWER = "PARAM_RF_MAXPOWER";
	}
	
	/**
	 * 读写器最小输出功率(只读)
	 */
	public final static class RF_MINPOWER{
		public final static String KEY = "RF_MINPOWER";
		/**读写器最小输出功率 */
		public final static String PARAM_RF_MINPOWER = "PARAM_RF_MINPOWER";
	}
	
	/**
	 * 标签过滤器
	 */
	public final static class TAG_FILTER{
		public final static String KEY = "TAG_FILTER";
		public final static String PARAM_TAG_FILTER = "PARAM_TAG_FILTER";
	    /**移除过滤器*/
	    public final static String PARAM_CLEAR = "PARAM_CLEAR";
	}
	
	/**
	 * 在进行标签的盘存操作的同时可以读某个分区的数据
	 */
	public final static class TAG_EMBEDEDDATA{
		public final static String KEY = "TAG_EMBEDEDDATA";
		 public final static String PARAM_TAG_EMBEDEDDATA = "PARAM_TAG_EMBEDEDDATA";
	    
	    /**移除附加设置项*/
	    public final static String PARAM_CLEAR = "PARAM_CLEAR";
	}
	
	/**
	 * 设置盘存操作的协议（仅仅M6e架构的读写器支持的参数）
	 */
	public final static class TAG_INVPOTL{
		public final static String KEY = "TAG_INVPOTL";

		/**盘存操作的协议*/
	    public final static String PARAM_TAG_INVPOTL = "PARAM_TAG_INVPOTL";
	}
	
	/**
	 * 所有被检测到的天线（不是所有的天线都能被检测）(只读)
	 */
	public final static class READER_CONN_ANTS{
		public final static String KEY = "READER_CONN_ANTS";
		/**所有被检测到的天线*/
		public final static String PARAM_CONNECT_ANTS = "PARAM_CONNECT_ANTS";
	}
	
	/**
	 * 返回读写器的天线端口数(只读)
	 */
	public final static class READER_AVAILABLE_ANTPORTS{
		public final static String KEY = "READER_AVAILABLE_ANTPORTS";
		/**读写器的天线端口数(*/
		public final static String PARAM_READER_AVAILABLE_ANTPORTS = "PARAM_READER_AVAILABLE_ANTPORTS";
	}
	
	/**
	 * 发射功率前要检测天线是否连接
	 */
	public final static class READER_IS_CHK_ANT{
		public final static String KEY = "READER_IS_CHK_ANT";
		/**发射功率前要检测天线是否连接*/
		public final static String PARAM_READER_IS_CHK_ANT = "PARAM_READER_IS_CHK_ANT";
	}
	
	/**
	 * 读写器ip地址
	 */
	public final static class READER_IP{
	    
		public final static String KEY = "READER_IP";
		
		public final static String PARAM_READER_IP = "PARAM_READER_IP";
	}
	
	/**
	 * 读写器工作区域
	 */
	public final static class FREQUENCY_REGION{
		public final static String KEY = "FREQUENCY_REGION";
		/**读写器工作区域*/
		public final static String PARAM_FREQUENCY_REGION = "PARAM_FREQUENCY_REGION";
	}
	
	/**
	 * 读写器跳频表设置
	 */
	public final static class FREQUENCY_HOPTABLE{
		public final static String KEY = "FREQUENCY_HOPTABLE";
		/**用于存储频点*/
	    public final static String PARAM_HTB = "PARAM_HTB";
	    
	}
	
	/**
	 * Gen2协议后向链路速率
	 */
	public final static class POTL_GEN2_BLF{
		public final static String KEY = "POTL_GEN2_BLF";
		/**Gen2协议后向链路速率*/
	    public final static String PARAM_POTL_GEN2_BLF = "PARAM_POTL_GEN2_BLF";
	}
	
	/**
	 * Gen2协议写模式
	 */
	public final static class POTL_GEN2_WRITEMODE{
		public final static String KEY = "POTL_GEN2_WRITEMODE";
		/**Gen2协议写模式*/
	    public final static String PARAM_POTL_GEN2_WRITEMODE = "PARAM_POTL_GEN2_WRITEMODE";
	}
	
	/**
	 * Gen2协议目标
	 */
	public final static class POTL_GEN2_TARGET{
		public final static String KEY = "POTL_GEN2_TARGET";
		/**用于存储频点*/
	    public final static String PARAM_POTL_GEN2_TARGET = "PARAM_POTL_GEN2_TARGET";
	}
	
	/**
	 * 对于同一个标签，如果被不同的天线读到是否将做为多条标签数据<p>
	 * 0表示同一个标签不论都多少个天线读到都作为一条标签数据；<p>
	 * 1表示同一个标签被不同的天线读到将作为多条标签数据<p>
	 */
	public final static class TAGDATA_UNIQUEBYANT{
		public final static String KEY = "TAGDATA_UNIQUEBYANT";
		/**不同的天线读到是否将做为多条标签数据*/
	    public final static String PARAM_TAGDATA_UNIQUEBYANT = "PARAM_TAGDATA_UNIQUEBYANT";
	}
	
	/**
	 * Epc相同的标签如果在使用嵌入盘存读功能时，读出的其它bank数据不同，是否作为多条标签数据
	 */
	public final static class TAGDATA_UNIQUEBYEMDDATA{
		public final static String KEY = "TAGDATA_UNIQUEBYEMDDATA";
		/**Epc相同的标签如果在使用嵌入盘存读功能时，读出的其它bank数据不同，是否作为多条标签数据*/
	    public final static String PARAM_TAGDATA_UNIQUEBYEMDDATA = "PARAM_TAGDATA_UNIQUEBYEMDDATA";
	}
	
	/**
	 * 是否只记录最大rssi
	 */
	public final static class TAGDATA_RECORDHIGHESTRSSI{
		public final static String KEY = "TAGDATA_RECORDHIGHESTRSSI";
		/**是否只记录最大rssi*/
	    public final static String PARAM_TAGDATA_RECORDHIGHESTRSSI = "PARAM_TAGDATA_RECORDHIGHESTRSSI";
	}
	
	/**
	 * 跳频时间
	 */
	public final static class RF_HOPTIME{
		public final static String KEY = "RF_HOPTIME";
		/**跳频时间*/
	    public final static String PARAM_RF_HOPTIME = "PARAM_RF_HOPTIME";
	}
	
	/**
	 * 是否启用lbt
	 */
	public final static class RF_LBT_ENABLE{
		public final static String KEY = "RF_LBT_ENABLE";
		/**是否启用lbt*/
	    public final static String PARAM_RF_LBT_ENABLE = "PARAM_RF_LBT_ENABLE";
	}
	
	/**
	 * 180006b协议后向链路速率
	 */
	public final static class POTL_ISO180006B_BLF{
		public final static String KEY = "POTL_ISO180006B_BLF";
		/**180006b协议后向链路速率*/
	    public final static String PARAM_POTL_ISO180006B_BLF = "PARAM_POTL_ISO180006B_BLF";
	}
	
	/**
	 * Gen2协议Tari
	 */
	public final static class POTL_GEN2_TARI{
		public final static String KEY = "POTL_GEN2_TARI";
		/**Gen2协议Tari*/
	    public final static String PARAM_POTL_GEN2_TARI = "PARAM_POTL_GEN2_TARI";
	}
	
	/**天线数据*/
    public final static class ANTS {
    	public final static String KEY = "ANTS";
		/**天线集合*/
	    public final static String PARAM_ANTS_GROUP= "PARAM_ANTS_GROUP";
	    /**操作的天线*/
	    public final static String PARAM_OPERATE_ANTS = "PARAM_OPERATE_ANTS";
	    /**支持的天线最大数(只读)*/
	    public final static String PARAM_MAX_ANTS_COUNT = "PARAM_MAX_ANTS_COUNT";
    }
    
    /**读写器省电模式*/
    public final static class POWERSAVE_MODE {
    	public final static String KEY = "POWERSAVE_MODE";
    	/**省电级别为0-3,数字越大表示越省电，0为完全不省电*/
    	public final static String PARAM_POWERSAVE_MODE = "PARAM_POWERSAVE_MODE";
    }
    
    /**盘点超时时间ms*/
    public final static class INV_TIME_OUT {
    	public final static String KEY = "INV_TIME_OUT";
		/**盘点超时时间*/
	    public final static String PARAM_INV_TIME_OUT = "PARAM_INV_TIME_OUT";
	    /**默认值盘点超时时间ms*/
	    public final static long DEFAULT_INV_TIMEOUT = 50L;
    }
	
    /**盘点间隔时间*/
    public final static class INV_INTERVAL{
    	public final static String KEY = "INV_INTERVAL";
    	/**盘点间隔时间*/
	    public final static String PARAM_INV_INTERVAL_TIME = "PARAM_INV_INTERVAL_TIME";
	    /**默认值盘点间隔时间ms*/
	    public final static long DEFAULT_INV_INTERVAL_TIME = 0L;
    }
    
    /**快速模式*/
    public final static class INV_QUICK_MODE{
    	
    	public final static String KEY = "INV_QUICK_MODE";
    	/**快速模式 1:开启,0:关闭*/
	    public final static String PARAM_INV_QUICK_MODE = "PARAM_INV_QUICK_MODE";
    }

	/**E7快速模式(UR90_V2.0[芯联创：MODOULE_SIM7100] 开始支持)*/
	public final static class INV_QUICK_MODE_EX{

		public final static String KEY = "INV_QUICK_MODE_EX";

		/**快速模式 */
		//0：普通模式
		//1：单标签手持机模式
		//2：多标签快速模式
		//3：多标签手持机模式
		//4：多标签智能（控温）模式
		//5：多标签测试模式
		//6：E7智能模式
		//7：E7智能模式v2
		//8：E7新快速模式
		public final static String PARAM_INV_QUICK_MODE = "PARAM_INV_QUICK_MODE";
	}

	/**Smart mode (UR90_V2.0[芯联创：MODOULE_SIM7100]开始支持)*/
	public final static class INV_SMART_MODE{

		public final static String KEY = "INV_SMART_MODE";

		/**Smart mode */
		//0：IT_MODE_CT,
		//1：IT_MODE_S2,
		//2：IT_MODE_E7,
		//3：IT_MODE_E7v2;
		public final static String PARAM_INV_SMART_MODE = "PARAM_INV_SMART_MODE";
	}

	/**Smart mode 的详细参数项 (UR90_V2.0[芯联创：MODOULE_SIM7100]开始支持)*/
	public final static class INV_SMART_MODE_PARAMS{

		public final static String KEY = "INV_SMART_MODE_PARAMS";

		/**Smart mode params */
		public final static String PARAM_INV_SMART_MODE_PARAMS = "PARAM_INV_SMART_MODE_PARAMS";
	}

	/**盘点模式，0-普通模式，1-高速模式*/
	public final static class TAG_SEARCH_MODE{
		public final static String KEY = "TAG_SEARCH_MODE";

		/**Smart mode params */
		public final static String PARAM_TAG_SEARCH_MODE = "PARAM_TAG_SEARCH_MODE";
	}

	/**模块相关信息（如：模块型号，硬件版本，软件版本等）*/
	public final static class MODULE_INFO{
		public final static String KEY = "MODULE_INFO";

		/**Module info params */
		public final static String PARAM_MODULE_INFO = "PARAM_MODULE_INFO";
	}

    /**温度*/
    public final static class TEMPTURE{
    	
    	public final static String KEY = "TEMPTURE";
    	/**温度*/
	    public final static String PARAM_TEMPTURE = "PARAM_TEMPTURE";
    }
    
    /**
     * 低电量时,读功率设置参数
     */
    public final static class LOWER_POWER{
    	
    	public final static String KEY = "LOWER_POWER";
    	/**是否开启低电量时,功率自动调节*/
    	public final static String PARAM_LOWER_POWER_DM_ENABLE = "PARAM_LOWER_POWER_DM_ENABLE";
    	/**低电量标准参数*/
    	public final static String PARAM_LOWER_POWER_LEVEL = "PARAM_LOWER_POWER_LEVEL";
    	/**读功率设置参数*/
	    public final static String PARAM_LOWER_POWER_READ_DBM = "PARAM_LOWER_POWER_DBM";
    }

	/**
	 * 电量警告（到指定的电量时警告提示）
	 */
	public final static class BATTERY_WARNING{
		public final static String KEY = "BATTERY_WARNING";
		//启用电量监视
		public final static String PARAM_BATTERY_WARNING_ENABLE = "PARAM_BATTERY_WARNING_ENABLE";
		//警告1的电量
		public final static String PARAM_BATTERY_WARNING_1 = "PARAM_BATTERY_WARNING_1";
		//警告2的电量
		public final static String PARAM_BATTERY_WARNING_2 = "PARAM_BATTERY_WARNING_2";
	}

	/**
	 * SIM7000组合盘点策略：
	 * 均衡模式：31dBm+多标签智能(控温)模式
	 * 高性能模式：33dBm+多标签智能(控温)模式
	 */
	public final static class INV_POLICY{

		public final static String KEY = "INV_POLICY";

		public final static String PARAM_INV_POLICY = "PARAM_INV_POLICY";

	}//End INV_POLICY

	/**
	 * 固件升级
	 */
	public final static class FIRMWARE_UPDATE{

		public final static String KEY = "FIRMWARE_UPDATE";

		public final static String PARAM_FIRMWARE_UPDATE = "PARAM_FIRMWARE_UPDATE";

	}//End FIRMWARE_UPDATE

	public final static class INV_FIELD_RSSI{

		public final static String KEY = "INV_FIELD_RSSI";

		public final static String PARAM_INV_FIELD_RSSI = "PARAM_INV_FIELD_RSSI";

	}//End INV_FIELD_RSSI

	public final static class INV_FIELD_FREQUENCE{

		public final static String KEY = "INV_FIELD_FREQUENCE";

		public final static String PARAM_INV_FIELD_FREQUENCE = "PARAM_INV_FIELD_FREQUENCE";

	}//End INV_FIELD_FREQUENCE

	public final static class INV_FIELD_PROTOCAL{

		public final static String KEY = "INV_FIELD_PROTOCAL";

		public final static String PARAM_INV_FIELD_PROTOCAL = "PARAM_INV_FIELD_PROTOCAL";

	}//End INV_FIELD_PROTOCAL

	/**
	 * Fastid
	 */
	public final static class FAST_ID{

		public final static String KEY = "FAST_ID";

		public final static String PARAM_FAST_ID = "PARAM_FAST_ID";

	}//End FAST_ID

	/**
	 * Output mode and bank
	 * 1-Auto fill
	 * 2-Emulate key
	 */
	public final static class EXTEND_OUTPUT_MODE{

		public final static String KEY = "EXTEND_OUTPUT_MODE";

		public final static String PARAM_EXTEND_OUTPUT_MODE = "PARAM_EXTEND_OUTPUT_MODE";

		public final static String PARAM_EXTEND_OUTPUT_MODE_BANK = "PARAM_EXTEND_OUTPUT_MODE_BANK";

		public final static int VALUE_OUTPUT_MODE_NONE = -1;

		public final static int VALUE_OUTPUT_MODE_AUTO_FILL = 1;

		public final static int VALUE_OUTPUT_MODE_EMULATE_KEY = 2;

	}//End OUTPUT_MODE

	/**
	 * Emulate keycode on send by 'Auto fill' or 'Emulate key'
	 */
	public final static class OUTPUT_CUSTOM_EMULATE_KEY{

		public final static String KEY = "OUTPUT_CUSTOM_EMULATE_KEY";

		public final static String PARAM_OUTPUT_CUSTOM_EMULATE_KEY = "PARAM_OUTPUT_CUSTOM_EMULATE_KEY";

		public final static int VALUE_EMULATE_KEYCODE_NONE = 0;

	}//End OUTPUT_CUSTOM_EMULATE_KEY

	/**
	 * Region certification （eg: China、North america、Europe、India、Kora、Japan）
	 */
	public final static class REGION_CERTIFICATION{

		public final static String KEY = "REGION_CERTIFICATION";

		public final static String PARAM_REGION_CERTIFICATION = "PARAM_REGION_CERTIFICATION";

	}//End REGION_CERTIFICATION

	/**
	 * High temperature settings（As pull down ant power,change to normal mode,when battery temperature higher than the sepecial value）
	 */
	public final static class HIGH_TEMPERATURE{

		public final static String KEY = "HIGH_TEMPERATURE";

		//Enable function
		public final static String PARAM_HIGH_TEMPERATURE_MONITOR_ENABLE = "PARAM_HIGH_TEMPERATURE_MONITOR_ENABLE";

		//Warning temperature
		public final static String PARAM_HIGH_TEMPERATURE_VALUE = "PARAM_HIGH_TEMPERATURE_VALUE";
		//Ant power
		public final static String PARAM_HIGH_TEMPERATURE_ANT_POWER = "PARAM_HIGH_TEMPERATURE_ANT_POWER";
		//Inventory strategy
		public final static String PARAM_HIGH_TEMPERATURE_INV_STRATEGY = "PARAM_HIGH_TEMPERATURE_INV_STRATEGY";

		//Inventory strategy values of UR90(SLR1200) on the high-temperature function
		public final static String VALUE_SLR1200_INV_POLICY_NORMAL = "normal";
		public final static String VALUE_SLR1200_INV_POLICY_QUICK_S0 = "q_s0";
		public final static String VALUE_SLR1200_INV_POLICY_QUICK_S1 = "q_s1";

		//Default temperature: 55
		public final static int DEFAULT_TEMPERATURE_VALUE = 55;
		//Defaut ant power: 2000
		public final static int DEFAULT_ANT_POWER_VALUE = 2000;

	}//End HIGH_TEMPERATURE

	/**Clear inventory cache*/
	public final static class INV_CLEAR_CACHE{

		public final static String KEY = "INV_CLEAR_CACHE";

		public final static String PARAM_INV_CLEAR_CACHE = "PARAM_INV_CLEAR_CACHE";
	}

	public static enum Region_Conf
	{
		RG_NONE(0),
		RG_NA(1), //北美
		RG_EU(2), //欧洲
		RG_EU2(7), //欧洲2
		RG_EU3(8), //欧洲3
		RG_KR(3), //韩国
		RG_PRC(6), //中国
		RG_PRC2(10), //中国２
		RG_OPEN(255),//全屏段

		RG_IN(4),
		RG_JP(5),
		RG_CE_HIGH(12),
		RG_HK(13),
		RG_TAIWAN(14),
		RG_MALAYSIA(15),
		RG_SOUTH_AFRICA(16),
		RG_BRAZIL(17),
		RG_THAILAND(18),
		RG_SINGAPORE(19),
		RG_AUSTRALIA(20),
		RG_URUGUAY(22),
		RG_VIETNAM(23),
		RG_ISRAEL(24),
		RG_PHILIPPINES(25),
		RG_INDONESIA(26),
		RG_NEW_ZEALAND(27),
		RG_PERU(28),
		RG_RUSSIA(29),
		RG_CE_LOW_HIGH(30);

		int p_v;

		private Region_Conf(int v) {
			this.p_v = v;
		}

		public int value() {
			return this.p_v;
		}

		public static Region_Conf valueOf(int value) {
			switch (value) {
				case 0:
					return RG_NONE;
				case 1:
					return RG_NA;
				case 2:
					return RG_EU;
				case 3:
					return RG_KR;
				case 4:
					return RG_IN;
				case 5:
					return RG_JP;
				case 6:
					return RG_PRC;
				case 7:
					return RG_EU2;
				case 8:
					return RG_EU3;
				case 10:
					return RG_PRC2;
				case 12:
					return RG_CE_HIGH;
				case 13:
					return RG_HK;
				case 14:
					return RG_TAIWAN;
				case 15:
					return RG_MALAYSIA;
				case 16:
					return RG_SOUTH_AFRICA;
				case 17:
					return RG_BRAZIL;
				case 18:
					return RG_THAILAND;
				case 19:
					return RG_SINGAPORE;
				case 20:
					return RG_AUSTRALIA;
				case 22:
					return RG_URUGUAY;
				case 23:
					return RG_VIETNAM;
				case 24:
					return RG_ISRAEL;
				case 25:
					return RG_PHILIPPINES;
				case 26:
					return RG_INDONESIA;
				case 27:
					return RG_NEW_ZEALAND;
				case 28:
					return RG_PERU;
				case 29:
					return RG_RUSSIA;
				case 30:
					return RG_CE_LOW_HIGH;
				case 255:
					return RG_OPEN;
				default:
					return null;
			}
		}
	}
}
