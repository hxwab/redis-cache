package test;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.*;

/**
 * Created by  xuwuhuang on 2018/1/12
 */
public final  class Test {

    private static  DatatypeFactory datatypeFactory   ;
    //static  XMLGregorianCalendar dateType = null;

    static {
       try {
           datatypeFactory = DatatypeFactory.newInstance();
       } catch (DatatypeConfigurationException e) {
           e.printStackTrace();
       }
   }


    public static void main(String[] args) {

        try {

            List<Integer> ids = new ArrayList<>();
            ids.add(1);
            ids.add(2);
            System.out.println(ids);

            Set<Integer> sets = new HashSet<>(ids);
            ids.add(3);
            System.out.println(sets);

            DatatypeFactory dtf = DatatypeFactory.newInstance();
            XMLGregorianCalendar dateType = dtf.newXMLGregorianCalendar();


            DatatypeFactory dtf1 = DatatypeFactory.newInstance();
            XMLGregorianCalendar dateType1 = dtf.newXMLGregorianCalendar();

            System.out.println(dtf == dtf1);
            System.out.println(dateType==dateType1);

            Calendar c = Calendar.getInstance();
            Calendar c1 = Calendar.getInstance();
            System.out.println(c==c1);

       /* for(int i =0;i<100000;i++){

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1);
                        System.out.println(toXMLGregorianCalendar(Calendar.getInstance()));
                    } catch (DatatypeConfigurationException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }

        while (true){

        }*/

        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }

    }



    public static XMLGregorianCalendar toXMLGregorianCalendar(Calendar calendar) throws DatatypeConfigurationException {
//       DatatypeFactory dtf = DatatypeFactory.newInstance();
        XMLGregorianCalendar  dateType = datatypeFactory.newXMLGregorianCalendar();
        dateType.setYear(calendar.get(Calendar.YEAR));
        //由于Calendar.MONTH取值范围为0~11,需要加1
        dateType.setMonth(calendar.get(Calendar.MONTH) + 1);
        dateType.setDay(calendar.get(Calendar.DAY_OF_MONTH));
        dateType.setHour(calendar.get(Calendar.HOUR_OF_DAY));
        dateType.setMinute(calendar.get(Calendar.MINUTE));
        dateType.setSecond(calendar.get(Calendar.SECOND));
        dateType.setMillisecond(calendar.get(Calendar.MILLISECOND));
        return dateType;
    }
}
