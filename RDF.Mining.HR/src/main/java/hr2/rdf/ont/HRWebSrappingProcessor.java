package hr2.rdf.ont;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
//import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;


public class HRWebSrappingProcessor {
	public static void main(String args[]) throws IOException{
		//WebDriver dr=getTextFromIndeed("https://www.indeed.com/r/soundarya-y/1057c75ec1afab1f?sp=0");
		
		File f = new File("C:\\Research\\done1.txt");

        System.out.println("Reading files using Apache IO:");

        List<String> lines = FileUtils.readLines(f, "UTF-8");
		for(int j=1;j<lines.size();j++){
			WebDriver dr=getTextFromIndeed(lines.get(j));			
			String work=getWorkExperience(dr);
			String education=getEducation(dr);
			String educationLevel=getEducationLevel(education);
			String educationName=getEducationName(education);
			Map<String,String> workText=splitWorkExperience(work);
			Collection<String> jobs=getJobs(workText); 
			String[] jobsArray=new String[jobs.size()];
			jobsArray=(String[])jobs.toArray(jobsArray);
			Set<String> startDate=getBeginDate(workText);
	
			String[] datesBegin=new String[startDate.size()];
			datesBegin=(String[])startDate.toArray(datesBegin);
		
			Set<String> finalDate=getEndDate(workText);
		
			String[] endDates=new String[finalDate.size()];
			endDates=(String[])finalDate.toArray(endDates);
		
			String skills=getSkills(dr);
			FileWriter writer = new FileWriter("C:\\Research\\output5.csv",true); // windows machine
			writer.write("Name_"+j+",");
			writer.write("Candidacy_"+j+",");
			writer.write("Education"+"_"+j+",");
			writer.write(educationLevel+",");
			writer.write(educationName+",");
			writer.write("Skill_of_"+j+",");
			writer.write(skills+"_"+j+",");
			
			for(int i=0;i<datesBegin.length;i++){
				if(datesBegin.length==endDates.length||datesBegin.length==jobsArray.length||endDates.length==jobsArray.length)
				if(i!=0)
				writer.write(",,,"+"Work_Experience"+j+"_"+i+","+datesBegin[i]+","+endDates[i]+","+jobsArray[i]+"\n");
				else
				writer.write("Work_Experience"+j+"_"+i+","+datesBegin[i]+","+endDates[i]+","+jobsArray[i]+"\n");
			
			}
				dr.quit();		
				writer.close();
		
		}
		


		
	}
	 
		static WebDriver getTextFromIndeed(String resume){
			System.setProperty("webdriver.gecko.driver", "C:\\geckodriver.exe");
	 		WebDriver dr = new FirefoxDriver();
	 		dr.get(resume);	 				
	 		return dr;
		}
		static String getWorkExperience(WebDriver dr){
			WebElement body = dr.findElement(By.id("work-experience-items"));
		
	 		String text=body.getText();
	 		Pattern p = Pattern.compile("(.*)(Responsibilities:)(.*)");
			Matcher m = p.matcher(text);
			if(m.find())
				return text;
			else return "  ";
			
		}
		
		static String getEducation(WebDriver dr){
			String toReturn=null;
			try{
			WebElement body = dr.findElement(By.id("education-items"));
	 		toReturn=body.getText();	
			}catch(Exception e){
				toReturn="none";
			}
	 		return toReturn;			
		}
		
		static String getSkills(WebDriver dr){
			String toReturn=null;
			try{
			WebElement body = dr.findElement(By.id("additionalinfo-items"));
			toReturn=body.getText();
			toReturn=toReturn.replace("\n", " ");
			toReturn=toReturn.replace(","," ");
			}catch(Exception e){
				toReturn="none";
			}
	 		return toReturn;			
		}
		
		static Map<String,String> splitWorkExperience(String text){
			
			//System.out.println(">>>>>>>>>>>>>> ::" + text);
			if(text.contains("Responsibilities:")){
			int startIdx = text.indexOf("Responsibilities:");
			int endIdx = 0;
			if(text.indexOf("Environment:")>startIdx){
				while(startIdx >=0 ){				
					endIdx = text.indexOf("Environment:", startIdx + 1);
					if (endIdx >=0 ){
						//System.out.println("startIdx = " + startIdx);
					//	System.out.println("endIdx =" + endIdx);
						String toRemoveString = text.substring(startIdx, endIdx);
						text = text.replace(toRemoveString,",");
					}
					//System.out.println(text);
					startIdx = text.indexOf("Responsibilities:");
				}
			}
			}
					
			String[] bucati=text.split("\n");
			StringBuilder builder=new StringBuilder();
			for(String i:bucati){
				//System.out.println(i);
				if(!i.toLowerCase().contains("environment")){					
					builder.append(i);
					builder.append(",");					
				}				
			}
			String x=builder.toString();
			
			Pattern p = Pattern.compile("(ianuarie.?|februarie.?|martie.?|aprilie.?|mai.?|iunie.?|iulie.?|august.?|septembrie.?|octombrie.?|noiembrie.?|decembrie.?)([0-9]{4}.?până la.?)(ianuarie.?|februarie.?|martie.?|aprilie.?|mai.?|iunie.?|iulie.?|august.?|septembrie.?|octombrie.?|noiembrie.?|decembrie.?|Prezent)([0-9]{4}|)");
			Matcher m = p.matcher(x);
			List<String> periods = new ArrayList<String>();
			
			while(m.find())
			{
			  String period = m.group( 0 ); //group 0 is always the entire match   
			  periods.add(period);
			}
			String[] periodsA=new String[periods.size()];
			periodsA=(String[]) periods.toArray(periodsA);
			/*
			for(String h:periods)
				System.out.println("perioada +"+h);
				*/
			String[] jobs=x.split("(ianuarie.?|februarie.?|martie.?|aprilie.?|mai.?|iunie.?|iulie.?|august.?|septembrie.?|octombrie.?|noiembrie.?|decembrie.?)([0-9]{4}.?până la.?)(ianuarie.?|februarie.?|martie.?|aprilie.?|mai.?|iunie.?|iulie.?|august.?|septembrie.?|octombrie.?|noiembrie.?|decembrie.?|Prezent)([0-9]{4}|)");
			/*
			for(String j:jobs)
				System.out.println("job+"+j);
				*/
			Map<String,String> results=new HashMap<String,String>();
			
			for(int i=0;i<periodsA.length;i++)
				results.put(periodsA[i], jobs[i]);
			return results;
			
		}
		static Collection<String> getJobs(Map<String,String> jobs){
			Collection<String> positions=jobs.values();
			Set<String> positions2=new TreeSet<String>();
			for(String s:positions){
				s=s.replace(",", " ");
				positions2.add(s);
				//System.out.println(s);
			}			
			return positions2;
		}
		
		static Set<String> getBeginDate(Map<String,String> jobs){
			Set<String> periods=new TreeSet<String>();
			periods=jobs.keySet();
			Set<String> beginDate=new TreeSet<String>();
			for(String s:periods){
				String y=s.substring(0, s.indexOf("până la"));
				y=y.replace(",", " ");
				beginDate.add(y);
				System.out.println(y);
			}
			
			return beginDate;
		}
		
		static Set<String> getEndDate(Map<String,String> jobs){
			Set<String> periods=new TreeSet<String>();
			periods=jobs.keySet();
			Set<String> endDate=new TreeSet<String>();
			for(String s:periods){
				String x=s.substring(s.indexOf("până la"), s.length());
				x=x.replace("până la", " ");
				x=x.replace(",", " ");
				endDate.add(x);
				System.out.println(x);
			}
			
			return endDate;
		}
		
		static String getEducationLevel(String text){
			//System.out.println(text);
			text.trim();
			String education=null;
			if(text.toLowerCase().contains("bachelor"))
				education="Bachelor";
			else if(text.toLowerCase().contains("master"))
				education="Master";
			else 
				education="-";
						
			//System.out.println("education ::" + education);
			return education;			
				
		}
		
		static String getEducationName(String text){
			String name=null;
			if(text.contains("Bachelor"))
				name=text.substring(text.lastIndexOf("Bachelor"), text.length()-1);
			else if(text.contains("Master of"))
				name=text.substring(text.lastIndexOf("Master of"), text.length()-1);
			else name="-";
			//System.out.println(name);
			String[] bucati=name.split("\n");
			StringBuilder builder=new StringBuilder();
			for(String s:bucati)
				if(s.toLowerCase().contains("bachelor")||s.toLowerCase().contains("master"))
					builder.append(s);
			name=builder.toString();
			return name;				
		}

		

	
	

}
