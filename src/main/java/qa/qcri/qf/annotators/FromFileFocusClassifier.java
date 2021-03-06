package qa.qcri.qf.annotators;



import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.TypeCapability;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

import qa.qcri.qf.type.QuestionClass;
import qa.qcri.qf.type.QuestionFocus;
import qa.qcri.qf.type.QuestionID;

import com.google.common.base.Charsets;
import com.google.common.io.Files;



public class FromFileFocusClassifier extends JCasAnnotator_ImplBase {
	private final Logger logger = LoggerFactory.getLogger(FromFileFocusClassifier.class);
	

	
	public static final String FOCUS_FILE = "focusFile";
	@ConfigurationParameter(name = FOCUS_FILE, mandatory = true)
	private String focusFile;

	private Map<String, String[]> questionIdToFocus;
	
	
	public static AnalysisEngineDescription getDescription(String categoriesFile) {
		AnalysisEngineDescription ann = null;
		try {
			ann = AnalysisEngineFactory.createPrimitiveDescription(FromFileFocusClassifier.class, FOCUS_FILE);
		} catch (ResourceInitializationException e) {
			e.printStackTrace();
		}
		return ann;
	}
	
	private void readQuestionCategories(String fname) throws IOException {
		for (String line : Files.readLines(new File(fname), Charsets.UTF_8)) {
			String[] data = line.trim().split("\t");
			questionIdToFocus.put(data[0].trim(), new String[]{data[1].trim(), data[2].trim()});
		}
	}
	
	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {		
		super.initialize(aContext);
		this.questionIdToFocus = new HashMap<String,String[]>();
		try {
			logger.debug("reading focus info from {}", focusFile);
			readQuestionCategories(focusFile);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void process(JCas cas) throws AnalysisEngineProcessException {
	
		String questionId = JCasUtil.selectSingle(cas, QuestionID.class).getQuestionID();
		
		//questionId = questionId.split("-")[1];
		
		
		String [] goldFocus = questionIdToFocus.get(questionId);
		

		
		int begin = Integer.valueOf(goldFocus[0]);
		int end = Integer.valueOf(goldFocus[1]);
		QuestionFocus annotation = new QuestionFocus(cas);
		annotation.setBegin(begin);
		annotation.setEnd(end);
		annotation.addToIndexes(cas);
		logger.info("Focus: '{}' for question: {}", annotation.getCoveredText(), questionId);
		
	}
}
