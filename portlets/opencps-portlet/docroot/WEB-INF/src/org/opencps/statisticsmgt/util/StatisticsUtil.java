
package org.opencps.statisticsmgt.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.opencps.statisticsmgt.bean.DossierStatisticsBean;
import org.opencps.statisticsmgt.model.DossiersStatistics;
import org.opencps.statisticsmgt.service.DossiersStatisticsLocalServiceUtil;
import org.opencps.statisticsmgt.service.persistence.DossiersStatisticsFinder;

import com.liferay.portal.kernel.dao.orm.SQLQuery;
import com.liferay.portal.kernel.dao.orm.Type;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.util.dao.orm.CustomSQLUtil;

public class StatisticsUtil {

	public static final String RECEIVED = "received";
	public static final String FINISHED = "finished";
	public static final String PROCESSING = "processing";

	public static final String STATISTICS_BY = "statisticsBy";
	public static final String MONTH = "month";
	public static final String YEAR = "year";

	public static enum StatisticsFieldNumber {
		ReceivedNumber, OntimeNumber, OvertimeNumber, ProcessingNumber,
			DelayingNumber, RemainingNumber
	}

	/**
	 * @param field
	 * @param delayStatus
	 * @return
	 */
	public static String getFilterCondition(String field, int... delayStatus) {

		String filter = StringPool.BLANK;
		StatisticsFieldNumber fieldNumber =
			StatisticsFieldNumber.valueOf(field);
		switch (fieldNumber) {
		case ReceivedNumber:
			filter =
				CustomSQLUtil.get(DossiersStatisticsFinder.class.getName() +
					StringPool.PERIOD + StringPool.OPEN_BRACKET + RECEIVED +
					StringPool.CLOSE_BRACKET);
			break;
		case OntimeNumber:
			filter =
				CustomSQLUtil.get(DossiersStatisticsFinder.class.getName() +
					StringPool.PERIOD + StringPool.OPEN_BRACKET + FINISHED +
					StringPool.CLOSE_BRACKET);
			break;
		case OvertimeNumber:
			filter =
				CustomSQLUtil.get(DossiersStatisticsFinder.class.getName() +
					StringPool.PERIOD + StringPool.OPEN_BRACKET + FINISHED +
					StringPool.CLOSE_BRACKET);
			break;

		case ProcessingNumber:
			filter =
				CustomSQLUtil.get(DossiersStatisticsFinder.class.getName() +
					StringPool.PERIOD + StringPool.OPEN_BRACKET + PROCESSING +
					StringPool.CLOSE_BRACKET);
			break;
		case DelayingNumber:
			filter =
				CustomSQLUtil.get(DossiersStatisticsFinder.class.getName() +
					StringPool.PERIOD + StringPool.OPEN_BRACKET + PROCESSING +
					StringPool.CLOSE_BRACKET);
			break;

		default:
			break;
		}
		return filter;
	}

	/**
	 * @param fieldName
	 * @return
	 */
	public static String getSetterMethodName(String fieldName) {

		String methodName = "set" + fieldName;
		return methodName;
	}

	/**
	 * @param q
	 * @param columnDataTypes
	 * @param cacheable
	 * @return
	 */
	public static SQLQuery bindingProperties(
		SQLQuery q, String[] columnDataTypes, boolean cacheable) {

		q.setCacheable(cacheable);

		if (columnDataTypes != null) {
			for (int i = 0; i < columnDataTypes.length; i++) {
				String columnName = "COL" + i;
				Type type = getDataType(columnDataTypes[i]);
				q.addScalar(columnName, type);
			}
		}

		return q;
	}

	/**
	 * @param typeLabel
	 * @return
	 */
	public static Type getDataType(String typeLabel) {

		return Type.valueOf(typeLabel.trim());
	}

	/**
	 * @param columnName
	 * @param coulmnDataType
	 * @param field
	 * @param delayStatus
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public static Method getMethod(
		String columnName, String coulmnDataType, String field,
		int... delayStatus)
		throws NoSuchMethodException, SecurityException,
		IllegalAccessException, IllegalArgumentException,
		InvocationTargetException {

		String methodName = "set";
		columnName = columnName.trim();
		coulmnDataType = coulmnDataType.trim();

		columnName =
			columnName.substring(0, columnName.indexOf(StringPool.SPACE));

		if (columnName.contains("d.")) {
			String temp =
				columnName.substring(
					columnName.lastIndexOf(StringPool.PERIOD) + 1,
					columnName.length());
			temp = StringUtil.upperCaseFirstLetter(temp);
			methodName += "Domain" + temp;
		}
		else if (columnName.contains("g.")) {
			String temp =
				columnName.substring(
					columnName.lastIndexOf(StringPool.PERIOD) + 1,
					columnName.length());
			temp = StringUtil.upperCaseFirstLetter(temp);
			methodName += "Gov" + temp;

		}
		else if (columnName.contains("count(opencps_processorder.processOrderId)")) {
			methodName = getSetterMethodName(field);
		}
		else {
			String temp =
				columnName.substring(
					columnName.lastIndexOf(StringPool.PERIOD) + 1,
					columnName.length());
			temp = StringUtil.upperCaseFirstLetter(temp);
			methodName += temp;
		}

		// System.out.println(methodName + "---" + columnName);

		Class<?> clazz = getClazz(coulmnDataType);

		Method method = null;
		try {
			if (clazz != null) {
				method =
					DossierStatisticsBean.class.getMethod(methodName, clazz);
			}
			else {
				method = DossierStatisticsBean.class.getMethod(methodName);
			}
		}
		catch (Exception e) {

		}

		return method;
	}

	/**
	 * @param typeLabel
	 * @return
	 */
	public static Class<?> getClazz(String typeLabel) {

		Class<?> clazz = null;
		Type type = Type.valueOf(typeLabel);
		switch (type) {

		case STRING:
			clazz = String.class;
			break;

		case LONG:
			clazz = long.class;
			break;

		case INTEGER:
			clazz = int.class;
			break;

		case DATE:
			clazz = Date.class;
			break;

		case SHORT:
			clazz = short.class;
			break;

		default:
			break;
		}
		return clazz;
	}

	/**
	 * @param map
	 * @return
	 */
	public static List<DossiersStatistics> getDossiersStatistics(List data) {

		List<DossiersStatistics> dossiersStatistics =
			new ArrayList<DossiersStatistics>();
		HashMap<String, List<DossierStatisticsBean>> statisticGroupByDomainMap =
			new HashMap<String, List<DossierStatisticsBean>>();
		HashMap<String, List<DossierStatisticsBean>> statisticGroupByGovMap =
			new HashMap<String, List<DossierStatisticsBean>>();
		HashMap<String, DossierStatisticsBean> statisticGovTreeIndexMap =
			new HashMap<String, DossierStatisticsBean>();
		HashMap<String, DossierStatisticsBean> beanMap =
			new HashMap<String, DossierStatisticsBean>();

		if (data != null) {
			_log.info("################################## data.size" +
				data.size());
			try {
				for (int i = 0; i < data.size(); i++) {
					DossierStatisticsBean statisticsBean =
						(DossierStatisticsBean) data.get(i);

					DossierStatisticsBean statisticsBeanTemp =
						new DossierStatisticsBean();

					String key =
						statisticsBean.getMonth() +
							StringPool.DASH +
							statisticsBean.getYear() +
							StringPool.DASH +
							(Validator.isNotNull(statisticsBean.getGovItemCode())
								? statisticsBean.getGovItemCode()
								: StringPool.BLANK) +
							StringPool.DASH +
							(Validator.isNotNull(statisticsBean.getDomainItemCode())
								? statisticsBean.getDomainItemCode()
								: StringPool.BLANK) + StringPool.DASH +
							statisticsBean.getAdministrationLevel();

					System.out.println("########################################################### key" +
						i +
						"--" +
						key +
						"{" +
						statisticsBean.getAdministrationLevel() +
						"}" +
						"{" +
						statisticsBean.getGroupId() + "}");

					if (beanMap != null && beanMap.containsKey(key)) {
						statisticsBeanTemp = beanMap.get(key);
					}

					// _log.info("###################################################statisticsBeanTemp "
					// + statisticsBeanTemp.getReceivedNumber() + "|" +
					// statisticsBeanTemp.getProcessingNumber() + "|" +
					// statisticsBeanTemp.getMonth());

					// Create Group (domain, gov, index != 0)

					statisticsBeanTemp.setGroupId(statisticsBean.getGroupId());

					statisticsBeanTemp.setCompanyId(statisticsBean.getCompanyId());

					statisticsBeanTemp.setUserId(statisticsBean.getUserId());

					if (statisticsBean.getDelayingNumber() > 0) {

						statisticsBeanTemp.setDelayingNumber(statisticsBean.getDelayingNumber());
					}

					if (statisticsBean.getOntimeNumber() > 0) {

						statisticsBeanTemp.setOntimeNumber(statisticsBean.getOntimeNumber());
					}

					if (statisticsBean.getOvertimeNumber() > 0) {

						statisticsBeanTemp.setOvertimeNumber(statisticsBean.getOvertimeNumber());
					}

					if (statisticsBean.getProcessingNumber() > 0) {

						statisticsBeanTemp.setProcessingNumber(statisticsBean.getProcessingNumber());
					}

					if (statisticsBean.getReceivedNumber() > 0) {

						statisticsBeanTemp.setReceivedNumber(statisticsBean.getReceivedNumber());
					}

					if (Validator.isNotNull(statisticsBean.getDomainItemCode())) {

						statisticsBeanTemp.setDomainItemCode(statisticsBean.getDomainItemCode());
					}

					if (Validator.isNotNull(statisticsBean.getGovItemCode())) {

						statisticsBeanTemp.setGovItemCode(statisticsBean.getGovItemCode());
					}

					statisticsBeanTemp.setAdministrationLevel(statisticsBean.getAdministrationLevel());

					statisticsBeanTemp.setGovTreeIndex(Validator.isNotNull(statisticsBean.getGovTreeIndex())
						? statisticsBean.getGovTreeIndex() : StringPool.BLANK);

					statisticsBeanTemp.setDomainTreeIndex(Validator.isNotNull(statisticsBean.getDomainTreeIndex())
						? statisticsBean.getDomainTreeIndex()
						: StringPool.BLANK);

					statisticsBeanTemp.setMonth(statisticsBean.getMonth());
					statisticsBeanTemp.setYear(statisticsBean.getYear());

					beanMap.put(key, statisticsBeanTemp);

				}

				if (beanMap != null) {
					for (String key : beanMap.keySet()) {
						DossierStatisticsBean dossierStatisticsBean =
							beanMap.get(key);

						int remainingNumber =
							dossierStatisticsBean.getProcessingNumber() +
								dossierStatisticsBean.getDelayingNumber() +
								dossierStatisticsBean.getOntimeNumber() +
								dossierStatisticsBean.getOvertimeNumber() -
								dossierStatisticsBean.getReceivedNumber();
						
						if(remainingNumber < 0){
							remainingNumber = 0;
						}

						dossierStatisticsBean.setRemainingNumber(remainingNumber);

						// add domain gov level !=0
						DossiersStatistics dossierStatistics =
							addDossiersStatistics(dossierStatisticsBean);

						if (Validator.isNotNull(dossierStatisticsBean.getDomainItemCode())) {
							String statisticGroupByDomainKey =
								dossierStatisticsBean.getDomainItemCode() +
									StringPool.DASH +
									dossierStatisticsBean.getMonth() +
									StringPool.DASH +
									dossierStatisticsBean.getYear();

							List<DossierStatisticsBean> dossierStatisticsBeansGroupByDomain =
								new ArrayList<DossierStatisticsBean>();

							if (statisticGroupByDomainMap != null &&
								statisticGroupByDomainMap.containsKey(statisticGroupByDomainKey)) {

								dossierStatisticsBeansGroupByDomain =
									statisticGroupByDomainMap.get(statisticGroupByDomainKey);

							}

							dossierStatisticsBeansGroupByDomain.add(dossierStatisticsBean);

							statisticGroupByDomainMap.put(
								statisticGroupByDomainKey,
								dossierStatisticsBeansGroupByDomain);
						}

						if (Validator.isNotNull(dossierStatisticsBean.getGovItemCode())) {
							String statisticGovTreeIndexKey =
								StringPool.PERIOD +
									dossierStatisticsBean.getMonth() +
									StringPool.DASH +
									dossierStatisticsBean.getYear() +
									StringPool.DASH +
									(dossierStatisticsBean.getGovTreeIndex().lastIndexOf(
										StringPool.PERIOD) + 1 == dossierStatisticsBean.getGovTreeIndex().length()
										? dossierStatisticsBean.getGovTreeIndex()
										: dossierStatisticsBean.getGovTreeIndex() +
											StringPool.PERIOD);

							statisticGovTreeIndexMap.put(
								statisticGovTreeIndexKey, dossierStatisticsBean);
						}

						beanMap.put(key, dossierStatisticsBean);

						dossiersStatistics.add(dossierStatistics);

					}
				}

				// Create Groups (domain, 0, index = 0)

				if (statisticGroupByDomainMap != null) {
					for (String key : statisticGroupByDomainMap.keySet()) {
						List<DossierStatisticsBean> dossierStatisticsBeans =
							statisticGroupByDomainMap.get(key);
						if (dossierStatisticsBeans != null) {
							DossierStatisticsBean dossierStatisticsBeanTemp =
								new DossierStatisticsBean();
							for (DossierStatisticsBean dossierStatisticsBean : dossierStatisticsBeans) {
								dossierStatisticsBeanTemp.setAdministrationLevel(0);
								dossierStatisticsBeanTemp.setCompanyId(dossierStatisticsBean.getCompanyId());
								dossierStatisticsBeanTemp.setDelayingNumber(dossierStatisticsBeanTemp.getDelayingNumber() +
									dossierStatisticsBean.getDelayingNumber());
								dossierStatisticsBeanTemp.setDomainItemCode(dossierStatisticsBean.getDomainItemCode());
								dossierStatisticsBeanTemp.setDomainTreeIndex(dossierStatisticsBean.getDomainTreeIndex());
								dossierStatisticsBeanTemp.setGovItemCode(StringPool.BLANK);
								dossierStatisticsBeanTemp.setGroupId(dossierStatisticsBean.getGroupId());
								dossierStatisticsBeanTemp.setGovTreeIndex(StringPool.BLANK);
								dossierStatisticsBeanTemp.setMonth(dossierStatisticsBean.getMonth());
								dossierStatisticsBeanTemp.setOntimeNumber(dossierStatisticsBeanTemp.getOntimeNumber() +
									dossierStatisticsBean.getOntimeNumber());
								dossierStatisticsBeanTemp.setOvertimeNumber(dossierStatisticsBeanTemp.getOvertimeNumber() +
									dossierStatisticsBean.getOvertimeNumber());
								dossierStatisticsBeanTemp.setProcessingNumber(dossierStatisticsBeanTemp.getProcessingNumber() +
									dossierStatisticsBean.getProcessingNumber());
								dossierStatisticsBeanTemp.setReceivedNumber(dossierStatisticsBeanTemp.getReceivedNumber() +
									dossierStatisticsBean.getReceivedNumber());
								dossierStatisticsBeanTemp.setRemainingNumber(dossierStatisticsBeanTemp.getRemainingNumber() +
									dossierStatisticsBean.getRemainingNumber());
								dossierStatisticsBeanTemp.setUserId(dossierStatisticsBean.getUserId());
								dossierStatisticsBeanTemp.setYear(dossierStatisticsBean.getYear());
							}

							DossiersStatistics dossierStatistics =
								addDossiersStatistics(dossierStatisticsBeanTemp);
							dossiersStatistics.add(dossierStatistics);
						}
					}
				}

				// **************
				if (statisticGovTreeIndexMap != null) {

					for (String treeIndex : statisticGovTreeIndexMap.keySet()) {
						DossierStatisticsBean dossierStatisticsBean =
							statisticGovTreeIndexMap.get(treeIndex);
						List<DossierStatisticsBean> dossierStatisticsBeans =
							new ArrayList<DossierStatisticsBean>();

						for (String treeIndexTemp : statisticGovTreeIndexMap.keySet()) {

							if (treeIndex.equals(treeIndexTemp) ||
								treeIndexTemp.contains(treeIndex)) {
								DossierStatisticsBean dossierStatisticsBeanTemp =
									statisticGovTreeIndexMap.get(treeIndexTemp);

								dossierStatisticsBeans.add(dossierStatisticsBeanTemp);

								// _log.info("######################## Sise"
								// + dossierStatisticsBeans.size() + "|"
								// + treeIndex + "|" + treeIndexTemp);
							}

						}

						String key =
							dossierStatisticsBean.getMonth() +
								StringPool.DASH +
								dossierStatisticsBean.getYear() +
								StringPool.DASH +
								(Validator.isNotNull(dossierStatisticsBean.getGovItemCode())
									? dossierStatisticsBean.getGovItemCode()
									: StringPool.BLANK) +
								StringPool.DASH +
								(Validator.isNotNull(dossierStatisticsBean.getDomainItemCode())
									? dossierStatisticsBean.getDomainItemCode()
									: StringPool.BLANK) + StringPool.DASH +
								dossierStatisticsBean.getAdministrationLevel();
						statisticGroupByGovMap.put(key, dossierStatisticsBeans);
					}
				}

				// Create Groups (domain, gov, index = 0)

				if (statisticGroupByGovMap != null) {

					for (String key : statisticGroupByGovMap.keySet()) {
						List<DossierStatisticsBean> dossierStatisticsBeans =
							statisticGroupByGovMap.get(key);

						if (dossierStatisticsBeans != null) {
							DossierStatisticsBean dossierStatisticsBeanTemp =
								new DossierStatisticsBean();
							for (DossierStatisticsBean dossierStatisticsBean : dossierStatisticsBeans) {

								dossierStatisticsBeanTemp.setAdministrationLevel(0);
								dossierStatisticsBeanTemp.setCompanyId(dossierStatisticsBean.getCompanyId());
								dossierStatisticsBeanTemp.setDelayingNumber(dossierStatisticsBeanTemp.getDelayingNumber() +
									dossierStatisticsBean.getDelayingNumber());
								dossierStatisticsBeanTemp.setDomainItemCode(dossierStatisticsBean.getDomainItemCode());
								dossierStatisticsBeanTemp.setDomainTreeIndex(dossierStatisticsBean.getDomainTreeIndex());
								dossierStatisticsBeanTemp.setGovItemCode(dossierStatisticsBean.getGovItemCode());
								dossierStatisticsBeanTemp.setGroupId(dossierStatisticsBean.getGroupId());
								dossierStatisticsBeanTemp.setGovTreeIndex(dossierStatisticsBean.getGovTreeIndex());
								dossierStatisticsBeanTemp.setMonth(dossierStatisticsBean.getMonth());
								dossierStatisticsBeanTemp.setOntimeNumber(dossierStatisticsBeanTemp.getOntimeNumber() +
									dossierStatisticsBean.getOntimeNumber());
								dossierStatisticsBeanTemp.setOvertimeNumber(dossierStatisticsBeanTemp.getOvertimeNumber() +
									dossierStatisticsBean.getOvertimeNumber());
								
								dossierStatisticsBeanTemp.setProcessingNumber(dossierStatisticsBeanTemp.getProcessingNumber() +
									dossierStatisticsBean.getProcessingNumber());
								dossierStatisticsBeanTemp.setReceivedNumber(dossierStatisticsBeanTemp.getReceivedNumber() +
									dossierStatisticsBean.getReceivedNumber());

								dossierStatisticsBeanTemp.setRemainingNumber(dossierStatisticsBeanTemp.getRemainingNumber() +
									dossierStatisticsBean.getRemainingNumber());
								dossierStatisticsBeanTemp.setUserId(dossierStatisticsBean.getUserId());
								dossierStatisticsBeanTemp.setYear(dossierStatisticsBean.getYear());
								
								dossierStatisticsBeanTemp.setDomainItemCode(StringPool.BLANK);
							}

							DossiersStatistics dossierStatistics =
								addDossiersStatistics(dossierStatisticsBeanTemp);
							dossiersStatistics.add(dossierStatistics);
						}
					}
				}

			}
			catch (Exception e) {
				_log.error(e);
			}
		}

		return dossiersStatistics;
	}

	/**
	 * @param dossierStatisticsBean
	 * @return
	 * @throws SystemException
	 * @throws PortalException
	 */
	public static DossiersStatistics addDossiersStatistics(
		DossierStatisticsBean dossierStatisticsBean)
		throws SystemException, PortalException {

		Date now = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(now);

		int currentYear = calendar.get(Calendar.YEAR);

		int currentMonth = calendar.get(Calendar.MONTH) + 1;

		_log.info("currentYear | currentMonth " + currentMonth + " | " +
			currentYear);

		DossiersStatistics dossierStatistics = null;

		if (dossierStatisticsBean.getMonth() == currentMonth &&
			dossierStatisticsBean.getYear() == currentYear) {
			try {
				dossierStatistics =
					DossiersStatisticsLocalServiceUtil.getDossiersStatisticsByG_GC_DC_M_Y_L(
						dossierStatisticsBean.getGroupId(),
						Validator.isNotNull(dossierStatisticsBean.getGovItemCode())
							? dossierStatisticsBean.getGovItemCode()
							: StringPool.BLANK,
						Validator.isNotNull(dossierStatisticsBean.getDomainItemCode())
							? dossierStatisticsBean.getDomainItemCode()
							: StringPool.BLANK,
						dossierStatisticsBean.getMonth(),
						dossierStatisticsBean.getYear(),
						dossierStatisticsBean.getAdministrationLevel());
			}
			catch (Exception e) {
				_log.info(e.getMessage());
			}
		}

		if (dossierStatistics == null) {
			dossierStatistics =
				DossiersStatisticsLocalServiceUtil.addDossiersStatistics(
					dossierStatisticsBean.getGroupId(),
					dossierStatisticsBean.getCompanyId(),
					dossierStatisticsBean.getUserId(),
					dossierStatisticsBean.getRemainingNumber(),
					dossierStatisticsBean.getReceivedNumber(),
					dossierStatisticsBean.getOntimeNumber(),
					dossierStatisticsBean.getOvertimeNumber(),
					dossierStatisticsBean.getProcessingNumber(),
					dossierStatisticsBean.getDelayingNumber(),
					dossierStatisticsBean.getMonth(),
					dossierStatisticsBean.getYear(),
					dossierStatisticsBean.getGovItemCode(),
					dossierStatisticsBean.getDomainItemCode(),
					dossierStatisticsBean.getAdministrationLevel());
		}
		else {
			_log.info("Update statistic: " + dossierStatisticsBean.getMonth() +
				"|" + dossierStatisticsBean.getYear());
			dossierStatistics =
				DossiersStatisticsLocalServiceUtil.updateDossiersStatistics(
					dossierStatistics.getDossierStatisticId(),
					dossierStatisticsBean.getRemainingNumber(),
					dossierStatisticsBean.getReceivedNumber(),
					dossierStatisticsBean.getOntimeNumber(),
					dossierStatisticsBean.getOvertimeNumber(),
					dossierStatisticsBean.getProcessingNumber(),
					dossierStatisticsBean.getDelayingNumber());
		}

		return dossierStatistics;
	}

	/**
	 * @param dossiersStatistics
	 * @param language
	 * @return
	 */
	public static JSONArray statisticsDossierMonthly(
		List<DossiersStatistics> dossiersStatistics, Locale locale) {

		JSONArray datas = JSONFactoryUtil.createJSONArray();
		String[] names =
			new String[] {
				"remaining-number", "received-number", "ontime-number",
				"overtime-number", "processing-number", "delaying-number"
			};

		if (dossiersStatistics != null) {
			for (int n = 0; n < names.length; n++) {
				JSONArray months = JSONFactoryUtil.createJSONArray();
				JSONArray values = JSONFactoryUtil.createJSONArray();
				JSONObject data = JSONFactoryUtil.createJSONObject();
				for (DossiersStatistics statistics : dossiersStatistics) {

					months.put(String.valueOf(statistics.getMonth()) + "/" +
						statistics.getYear());

					if (names[n].equals("remaining-number")) {
						values.put(String.valueOf(statistics.getRemainingNumber()));
					}
					else if (names[n].equals("received-number")) {
						values.put(String.valueOf(statistics.getReceivedNumber()));
					}
					else if (names[n].equals("ontime-number")) {
						values.put(String.valueOf(statistics.getOntimeNumber()));
					}
					else if (names[n].equals("overtime-number")) {
						values.put(String.valueOf(statistics.getOvertimeNumber()));
					}
					else if (names[n].equals("processing-number")) {
						values.put(String.valueOf(statistics.getProcessingNumber()));
					}
					else if (names[n].equals("delaying-number")) {
						values.put(String.valueOf(statistics.getDelayingNumber()));
					}

				}

				data.put("name", LanguageUtil.get(locale, names[n]));
				data.put("months", months);
				data.put("values", values);
				datas.put(data);
			}

		}

		return datas;
	}

	/**
	 * @param startMonth
	 * @param startYear
	 * @param preriod
	 * @return
	 */
	public static LinkedHashMap<Integer, List<Integer>> getPeriodMap(
		int startMonth, int startYear, int period) {

		LinkedHashMap<Integer, List<Integer>> map =
			new LinkedHashMap<Integer, List<Integer>>();
		List<Integer> months = new ArrayList<Integer>();
		months.add(startMonth);
		map.put(startYear, months);
		for (int p = 1; p < period; p++) {
			int numberOfMonth = startMonth + p;
			if (numberOfMonth <= 12) {
				List<Integer> monthsTemp = map.get(startYear);
				monthsTemp.add(numberOfMonth);
				map.put(startYear, monthsTemp);
			}
			else {
				int year = (int) ((numberOfMonth - 1) / 12) + startYear;
				List<Integer> monthsTemp = new ArrayList<Integer>();
				if (map.containsKey(year)) {
					monthsTemp = map.get(year);
				}
				int month = numberOfMonth % 12 == 0 ? 12 : numberOfMonth % 12;
				monthsTemp.add(month);
				map.put(year, monthsTemp);
			}
		}

		return map;
	}

	public static String getPeriodConditions(
		int startMonth, int startYear, int period) {

		StringBuffer conditions = new StringBuffer();
		LinkedHashMap<Integer, List<Integer>> map =
			getPeriodMap(startMonth, startYear, period);
		if (map != null) {
			int count = 1;
			for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {

				List<Integer> months = entry.getValue();
				conditions.append(StringPool.OPEN_PARENTHESIS);
				conditions.append("opencps_dossierstatistics.month BETWEEN" +
					StringPool.SPACE);
				conditions.append(months.get(0));
				conditions.append(StringPool.SPACE + "AND" + StringPool.SPACE);
				conditions.append(months.get(months.size() - 1));
				conditions.append(StringPool.SPACE + "AND" + StringPool.SPACE);
				conditions.append("opencps_dossierstatistics.year = ");
				conditions.append(entry.getKey());
				conditions.append(StringPool.CLOSE_PARENTHESIS);
				if (count < map.size()) {
					conditions.append(StringPool.SPACE + "OR" +
						StringPool.SPACE);
				}

				count++;
			}
		}

		return conditions.toString();
	}
	
	public static String test(){
		return "AAAAAAAA";
	}

	/*
	 * public static void main(String[] args) { LinkedHashMap<Integer,
	 * List<Integer>> map = getPeriodMap(5, 2016, 37); for (Map.Entry<Integer,
	 * List<Integer>> entry : map.entrySet()) { List<Integer> months =
	 * entry.getValue(); System.out.println("Year: " + entry.getKey() + "[" +
	 * StringUtil.merge(months) + "]"); }
	 * System.out.println(getPeriodConditions(5, 2016, 37)); }
	 */

	private static Log _log =
		LogFactoryUtil.getLog(StatisticsUtil.class.getName());
}