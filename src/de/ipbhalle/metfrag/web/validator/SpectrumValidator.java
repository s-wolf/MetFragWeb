package de.ipbhalle.metfrag.web.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import com.icesoft.faces.context.effects.JavascriptContext;


public class SpectrumValidator implements Validator {
	private static final String PEAKLIST_PATTERN = "([0-9]*[\\.,]?[0-9]+\\s*){2,3}"; 
	
	private Pattern pattern;
	private Matcher matcher;
	
	public SpectrumValidator() {
		pattern = Pattern.compile(PEAKLIST_PATTERN);
	}
	
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		String[] lineArray = value.toString().split("\n");
		
		for (int i = 0; i < lineArray.length; i++) {
			matcher = pattern.matcher(lineArray[i].trim());
			if(!matcher.matches())
			{
				FacesMessage msg = new FacesMessage("Spectrum validation failed.", "Invalid Spectrum format. Error in line " + (i+1));
				JavascriptContext.addJavascriptCall(context, "document.getElementById(\"searchUpstream\").firstChild.disabled = true;");
				msg.setSeverity(FacesMessage.SEVERITY_ERROR);
				throw new ValidatorException(msg);	
			}
		}
		JavascriptContext.addJavascriptCall(context, "document.getElementById(\"searchUpstream\").firstChild.removeAttribute(\"disabled\");");
		System.out.println("Spectrum is valid");
	}
}