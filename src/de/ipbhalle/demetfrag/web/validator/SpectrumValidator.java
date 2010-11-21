package de.ipbhalle.demetfrag.web.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;


public class SpectrumValidator implements Validator {
	private static final String PEAKLIST_PATTERN_2COL = "([0-9]+([\\.|,][0-9]+)?(\\s)+[0-9]+([\\.|,][0-9]+)?(\\n|\\r)?)+"; 
		//"([0-9]+(\\.{1}[0-9]+)?\\s[0-9]+\\s)+";	//"([0-9]+.?[0-9]*\\s*[0-9]+\\s*[0-9]+)+\\s*";
//	private static final String PEAKLIST_PATTERN_3COL = "([0-9]+(\\.[0-9]+)?\\s[0-9]+(\\.[0-9]+)?\\s[0-9]+)+";
	
	private Pattern pattern_2col;
	private Pattern pattern_3col;
	private Matcher matcher;
	private Matcher matcher2;
	
	public SpectrumValidator() {
		pattern_2col = Pattern.compile(PEAKLIST_PATTERN_2COL);
//		pattern_3col = Pattern.compile(PEAKLIST_PATTERN_3COL);
	}
	
	public void validate(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
		System.out.println("SpectrumValidator -> validating \n" + value.toString());
//		matcher = pattern_3col.matcher(value.toString());
		matcher2 = pattern_2col.matcher(value.toString());
		
		if(/*!matcher.matches() && */ !matcher2.matches()) {
			System.out.println(/*"matcher -> " + matcher.matches() + */"  matcher2 -> " + matcher2.matches());
			System.err.println("spectrum doesnt match!");
			//FacesContext fc = FacesContext.getCurrentInstance();			
			FacesMessage msg = new FacesMessage("Spectrum validation failed.", "Invalid Spectrum format.");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			//fc.addMessage("inputForm:spec", msg);
			throw new ValidatorException(msg);
		}
		System.out.println("spectrum matches!");
	}
}