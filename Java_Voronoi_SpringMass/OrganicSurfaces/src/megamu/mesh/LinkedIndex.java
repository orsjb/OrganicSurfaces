package megamu.mesh;

import java.util.ArrayList;


public class LinkedIndex {

	LinkedArray array;
	int index;
	ArrayList<Integer> links;

	public LinkedIndex(LinkedArray a, int i){
		array = a;
		index = i;
		links = new ArrayList<Integer>();
	}

	public void linkTo(int i){
		links.add(i);
	}

	public boolean linked(int i){
		for(int l : links)
			if(l==i)
				return true;
		return false;
	}

	public ArrayList<Integer> getLinks(){
		return links;
	}

}