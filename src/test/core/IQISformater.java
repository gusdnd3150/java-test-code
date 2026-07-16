package test.core;

public class IQISformater {

    public static void main(String[] args) {

        String[] header;
        String headerDiv=":";
        String[] body;
        String bodyDiv = ",";
        String hbDiv = ";";
        String rslt ="";

        header = new String[]{"1","2","3","4"};
        body = new String[]{"5","6","7","8"};

        String hd = String.join(headerDiv, header);
        String bd = String.join(bodyDiv, body);
        rslt = String.join(hbDiv, hd,bd);

        System.out.println(String.format("main :: %s", rslt));
    }
}
