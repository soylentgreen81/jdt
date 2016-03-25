package de.beukmann.jdt.jpa;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.eclipse.jdt.apt.core.env.EclipseAnnotationProcessorEnvironment;
import org.eclipse.jdt.apt.core.util.EclipseMessager;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.Types;

public class EntityProcessorFactory implements AnnotationProcessorFactory {

	public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> declarations,
			AnnotationProcessorEnvironment env) {
		return new EntityAnnotationProcessor((EclipseAnnotationProcessorEnvironment) env);
	}

	public Collection<String> supportedAnnotationTypes() {
		return Arrays.asList(Entity.class.getName());
	}

	public Collection<String> supportedOptions() {
		return Collections.emptyList();
	}

	public static class EntityAnnotationProcessor implements AnnotationProcessor {
		private Types typeUtils;

		private final EclipseAnnotationProcessorEnvironment env;
		private final InterfaceType collectionType;
		private final EclipseMessager messager;
		public EntityAnnotationProcessor(EclipseAnnotationProcessorEnvironment env) {
			this.env = env;
			this.typeUtils = env.getTypeUtils();
			this.messager = env.getMessager();
			collectionType = (InterfaceType) env.getTypeDeclaration("java.util.Collection");
		}

		public void process() {
			for (TypeDeclaration td : env.getSpecifiedTypeDeclarations()) {
				if (td.getFields().stream().filter(f -> f.getAnnotation(Id.class) != null).count() == 0) {
					messager.printFixableError(td.getPosition(), String.format("No field annotated with @Id in entity class %s ", td.getQualifiedName()), Activator.PLUGIN_ID, "NO_ID_ERROR");
				}
				for (FieldDeclaration member : td.getFields()) {
					TypeMirror memberType = member.getType();
						if (isCollection(memberType)) {
							//Check if a collection is present
							DeclaredType declaredMemberType = (DeclaredType) memberType;
							DeclaredType firstTypeParameter = (DeclaredType) declaredMemberType.getActualTypeArguments().toArray()[0];
							if (firstTypeParameter instanceof ClassDeclaration){ 
								ClassDeclaration cd =  (ClassDeclaration) firstTypeParameter;
								if (cd.getAnnotation(Entity.class) == null){
									messager.printError(cd.getPosition(), String.format("%s is not a declared @Entity", cd.getQualifiedName()));
								}
							}
						}
				}
			}
		}

		private boolean isCollection(TypeMirror type) {
			TypeMirror erasedType = typeUtils.getErasure(type);
			if (typeUtils.isSubtype(erasedType, collectionType)) {
				return true;
			}
			return false;
		}
		
	}

}
