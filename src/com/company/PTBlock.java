package com.company;

public class PTBlock 
{
	static class ActionType
	{
		static final int Error = 0;
		static final int Shift = 1;
		static final int Goto = 2;
		static final int PushGoto = 3;
		static final int Reduce = 4;
		static final int Accept = 5;
	}
	
	private String _sem;

	//public String Sem;

	private int _index;

	//public int Index;

	private int _act;

	public String getSem() {
		return _sem;
	}

	public void setSem(String _sem) {
		this._sem = _sem;
	}

	public int getIndex() {
		return _index;
	}

	public void setIndex(int _index) {
		this._index = _index;
	}

	public int getAct() {
		return _act;
	}

	public void setAct(int _act) {
		this._act = _act;
	}

	//public ActionType Action;
	
}
