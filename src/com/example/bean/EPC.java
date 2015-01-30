package com.example.bean;

public class EPC {
	private int id;
	private String TID;
	private int count;
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the epc
	 */
	public String getTID() {
		return TID;
	}
	/**
	 * @param epc the epc to set
	 */
	public void setTID(String TID) {
		this.TID = TID;
	}
	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}
	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TID [id=" + id + ", TID=" + TID + ", count=" + count + "]";
	}
	
	
}
