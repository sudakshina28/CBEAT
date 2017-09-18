import java.io.File;
import java.io.FileReader;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.apache.commons.io.FileUtils;

public class analyzer {
	
    public static void main(String[] args) {
    	
    	System.out.println("Manifest File Analysis");
    	System.out.println("********************** \n");
    	
        JSONParser parser = new JSONParser(); 
        try {
        	
        	Object obj = parser.parse(new FileReader(
                    "D:\\Amrita_university\\3rd_Semester\\Manifest_Analysis\\emojikeyboard.json"));
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray permissions = (JSONArray) jsonObject.get("permissions");

            Object obj1 = parser.parse(new FileReader(
                    "D:\\Amrita_university\\3rd_Semester\\Manifest_Analysis\\output.json"));
            JSONObject jsonObject1 = (JSONObject) obj1;
            JSONArray permissions1 = (JSONArray) jsonObject1.get("permissions");
 
            ArrayList<String> list = new ArrayList<String>();     
            if (permissions1 != null) { 
               int len = (permissions1).size();
               for (int i=0;i<len;i++){ 
                list.add(permissions1.get(i).toString());
               } 
            } 
            
            ArrayList<String> list1 = new ArrayList<String>();     
            if (permissions != null) { 
               int len = (permissions).size();
               for (int i=0;i<len;i++){ 
                list1.add(permissions.get(i).toString());
               } 
            } 
            
            List<String> common = new ArrayList<String>(list1);
            common.retainAll(list);
            
            System.out.println("Permissions Analysis");
            System.out.println("********************\n");
            System.out.println("Permissions which can violate user privacy found in the extension:");
            Iterator<String> iterator2 = common.iterator();
            while (iterator2.hasNext()) {
                System.out.println(iterator2.next());
            }

            JSONObject obj2 = (JSONObject) jsonObject.get("background");
            System.out.println("\nPersistency Analysis");
            System.out.println("********************\n");
            
            try{
            boolean result = (boolean) obj2.get("persistent");
            
            if(result){
            	System.out.println("Extension runs persistently in background");
            }
            else{
            	System.out.println("Extension does not run in background");
            }
            }
            catch(NullPointerException e) {
            	System.out.println("Extension does not run persistently in background");
            }
            
            System.out.println("\nContent Script Analysis");
            System.out.println("*********************** \n");
            
            Object obje = null;
            ArrayList<String> list2 = new ArrayList<String>();
            JSONArray obj3 = (JSONArray) jsonObject.get("content_scripts");
            try{
            JSONObject obj4 = (JSONObject) obj3.get(0);
            
            
            JSONArray matches = (JSONArray) obj4.get("matches");     
                if (matches != null) { 
                   int len = (matches).size();
                   for (int i=0;i<len;i++){ 
                    list2.add(matches.get(i).toString());
                   } 
                }
            System.out.println("Content Scripts matches found to:");
            Iterator<String> iter = list2.iterator();
            while (iter.hasNext()) {
                System.out.println(iter.next());
            }
            }

            catch(NullPointerException e){
            	System.out.println("Content Scripts not found");
            }    
            
            System.out.println("\nChrome URL Override Analysis");
            System.out.println("**************************** \n");
            
            try{
            
            JSONObject obj5 = (JSONObject) jsonObject.get("chrome_url_overrides");
            
            String over = (String) obj5.get("newtab");
            String over1 = (String) obj5.get("history");
            String over2 = (String) obj5.get("bookmarks");
            
            if (over!=null){
            	System.out.println("The extension overrides new tab with: "+over);
            }
            if (over1!=null){
                System.out.println("The extension overrides history with: "+over);
            }
            if (over2!=null){
                System.out.println("The extension overrides bookmarks with: "+over);
            }
            
            
            }
            catch(NullPointerException e){
            	System.out.println("The extension does not override any Chrome URL");
            }
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try{
        	
        	File f = new File("D:\\Amrita_university\\3rd_Semester\\Manifest_Analysis\\popup.js");
        	//String s= FileUtils.readFileToString(f);
        	String str = FileUtils.readFileToString(f, "UTF-8");
        	String s="$.post";
        	if(str.contains(s)){
        		System.out.println("Found");
        	}

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}