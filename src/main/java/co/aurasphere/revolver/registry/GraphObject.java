package co.aurasphere.revolver.registry;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

public class GraphObject {
	
	private GraphObject parentElement;
	
	private TypeElement currentClass;
	
	private List<GraphObject> childElement;
	
	public GraphObject(ClassInfo info){
	}

}
