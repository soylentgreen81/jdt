package de.beukmann.jdt.jpa;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.apt.core.util.EclipseMessager;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;
import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;



public class JpaQuickFixProcessor implements IQuickFixProcessor {

	public boolean hasCorrections(ICompilationUnit unit, int problemId) {
		return problemId == EclipseMessager.APT_QUICK_FIX_PROBLEM_ID;
	}

	public IJavaCompletionProposal[] getCorrections(IInvocationContext context,
			IProblemLocation[] locations) throws CoreException {
		List<IJavaCompletionProposal> results = new ArrayList<IJavaCompletionProposal>();
		for (IProblemLocation location : locations){
			if (location.getProblemArguments()[1].equals("NO_ID_ERROR")){
				CompilationUnit astRoot = context.getASTRoot();
				
			    TypeDeclaration typeDecl = (TypeDeclaration) astRoot.types().get(0);
			    for (FieldDeclaration fieldDec : typeDecl.getFields()){
			    	if (fieldDec.getType().isPrimitiveType() || fieldDec.getType().isSimpleType()){
			    		ASTRewrite rewrite = ASTRewrite.create(astRoot.getAST());
			    		final ListRewrite listRewrite = rewrite.getListRewrite(fieldDec, fieldDec.getModifiersProperty());
				    	final NormalAnnotation eventHandlerAnnotation = astRoot.getAST().newNormalAnnotation();
					    eventHandlerAnnotation.setTypeName(astRoot.getAST().newName("javax.persistence.Id"));
					    listRewrite.insertAt(eventHandlerAnnotation, 0, null);							
					    results.add(new ASTRewriteCorrectionProposal("Add ID to " + fieldDec.toString() , context.getCompilationUnit(), rewrite, 0));
					    
			    	}
			    }
			}
		}
		
		return results.toArray(new IJavaCompletionProposal[0]);
	} 


}
