package hr2.rdf.ont;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

public class Indeed {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<String> links=getLinks();
		List<String> linksList=getLinksThatGetLinks(links);
		List<String> linksLinks=getLinksLinks(linksList);
		List<String> links2=getAddedLinks(linksLinks);
				
		for(String s:links2)
			System.out.println(s);
		
	}
		static List<String> getLinks(){
			System.setProperty("webdriver.gecko.driver", "C:\\geckodriver.exe");
			WebDriver dr = new FirefoxDriver();
			dr.get("https://www.indeed.com/resumes?q=java&l=");
			List <WebElement> alllinks = dr.findElements(By.tagName("a"));
			List<String> hrefs = new ArrayList<String>();
			for ( WebElement anchor : alllinks ) {
 		    hrefs.add(anchor.getAttribute("href"));
			}
 		return hrefs;
		}
		
		
		
		static List<String> getLinksLinks(List<String> links){
			List<String> result=new ArrayList<String>();
			WebDriver dr = new FirefoxDriver();
			for(String a:links){				
		 		dr.get(a);		 		
		 		List <WebElement> alllinks = dr.findElements(By.tagName("a"));
		 		List<String> hrefs = new ArrayList<String>();
		 		for ( WebElement anchor : alllinks ){
		 		    result.add(anchor.getAttribute("href"));		 		
		 		}
			}
			return result;
		}
		
		static List<String> getAddedLinks(List<String> links){
			List<String> linksToAdd=new ArrayList<String>();
			for(String a:links)
	 			if(a.endsWith("?sp=0"))
	 				linksToAdd.add(a);
			return linksToAdd;
		}
 		
		static List<String> removeLinks(List<String> links)	{			
 		List<String> linksToRemove=new ArrayList<String>();
 		for(String x:links){
 			if(x.endsWith("D"))
 				linksToRemove.add(x);
 			else if(x.equals("https://www.indeed.com/resumes")||x.contains("ads.indeed.com")||x.contains("ro.indeed.com")||x.contains("javascript:void(0)")||x.contains("www.indeed.com/resumes/advanced"))
 				linksToRemove.add(x);
 		}
 		links.removeAll(linksToRemove);
 		return links;
		}
		
		static List<String> getLinksThatGetLinks(List<String> links){
 		List<String> linksThatGetLinks=new ArrayList<String>();
 		for(String e:links)
 			if(e.contains("https://www.indeed.com/resumes?q="))
 				linksThatGetLinks.add(e);
 		return linksThatGetLinks;
		}
 		

	static List<String> getAllLinks(List<String> links){
		WebDriver dr = new FirefoxDriver();
 		dr.get("https://www.indeed.com/resumes?q=java&l=");
 		List <WebElement> alllinks = dr.findElements(By.tagName("a"));
 		List<String> hrefs = new ArrayList<String>();
 		for ( WebElement anchor : alllinks ) {
 		    hrefs.add(anchor.getAttribute("href"));
 		}
 		return hrefs;
	}

}
