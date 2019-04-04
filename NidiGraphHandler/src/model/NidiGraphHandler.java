package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.LinkTarget;
import guru.nidi.graphviz.model.MutableAttributed;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.parse.Parser;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;

public class NidiGraphHandler {
	private Field fieldExtractor=null;

	public MutableGraph mutableGraph;
	public String graphName = "Graph";
	public String projectName = "project";
	public String defaultSavePath = "example/";
	
	public NidiGraphHandler(String path) {
		mutableGraph = importFromDotFileUtf8(path);
	}
	
	public NidiGraphHandler(File file) {
		mutableGraph = importFromDotFileUtf8(file);
	}
	
	private void setNodeLable(MutableNode mn, String label) {
		MutableAttributed<?, ?> mutableAttributed = mn.attrs();
		mutableAttributed.add("label", label);
	}

	private void setLableOfLink(MutableNode startNode, int i, String label) {
		List <Link> ll = startNode.links();
		Link l = ll.get(i);
		MutableAttributed<?, ?> mutableAttributed = l.attrs();
		mutableAttributed.add("label", label);
	}

	private String getNodeLabel(MutableNode mn) {
		MutableAttributed<?, ?> mutableAttributed = mn.attrs();
		String label;
		Object ob = mutableAttributed.get("label");
		label =  ob==null? null: mutableAttributed.get("label").toString();
		return label;
	}
	
	private MutableNode getLinkSource(Link link) {
		MutableNode linkSource = (MutableNode) link.from();
		return linkSource;
	}
	
	private MutableNode getLinkTarget(Link link) {
		LinkTarget linkTarget = link.to();
		if (fieldExtractor==null)
			fieldExtractor = setAccessibilityToPrivateField(linkTarget.getClass(), "node");
		MutableNode targetNode;
		try {
			targetNode = (MutableNode) fieldExtractor.get(linkTarget);
			return targetNode;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private Field setAccessibilityToPrivateField(Class<?> claz, String fieldName) {
		try {
			Field fieldExtractor = claz.getDeclaredField(fieldName);
			fieldExtractor.setAccessible(true);
			return fieldExtractor;
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String getLinkLabel(Link link) {
		MutableAttributed<?, ?> mutableAttributed = link.attrs();
		String label;
		Object ob = mutableAttributed.get("label");
		label =  ob==null? null: mutableAttributed.get("label").toString();
		return label;
	}
	
	public MutableGraph importFromDotFileUtf8(String path) {
		File file = new File(path);
		MutableGraph g=importFromDotFileUtf8(file);
		return g;
	}
	
	public MutableGraph importFromDotFileUtf8(File file) {
		graphName = file.getName();
		int index = graphName.lastIndexOf(".");
		graphName = index>0 ? graphName.substring(0, index): graphName;
		
		projectName = file.getParentFile().getParentFile().getName();
		defaultSavePath += projectName + "/" + graphName;
		
		MutableGraph g=null;
		String s = readStringTextFileUtf8(file.getAbsolutePath());
		try {s = s.replaceAll("cluster_.", "cluster_p");
			g = Parser.read(s);

		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Loaded: " + file.getAbsolutePath());
		return g;
	}
	
	public String readStringTextFileUtf8(String path) {
        StringBuilder res = new StringBuilder();
		List<String> linesList = readLinesTextFileUtf8(path);
		for (String line : linesList) {
        	res.append(line).append("\n");
		}
        return res.toString();
    }
	

	public List<String> readLinesTextFileUtf8(String path) {
		File f = new File(path);
		InputStreamReader inputStreamReader = null;
		List<String> linesList = new ArrayList<String>();
		try {
			inputStreamReader = new InputStreamReader(new FileInputStream(f), "UTF-8");
		} catch (UnsupportedEncodingException | FileNotFoundException e1) {
			e1.printStackTrace();
			return linesList;
		}
		String line;
        try (BufferedReader bufferedReader = new BufferedReader(
        		inputStreamReader)) {
            while ((line = bufferedReader.readLine()) != null) {
            	linesList.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return linesList;
    }
	
	public String saveToFile() {
		String path = defaultSavePath +".png";
		return saveToFile(path);
	}
	
	public String saveToFile(String path) {
		File file = new File(path);
		if (!file.getName().contains("."))
			file = new File(path, defaultSavePath +".png");
		try {
			Graphviz.fromGraph(mutableGraph).render(Format.PNG).toFile(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file.getAbsolutePath();
	}
	
	
}
