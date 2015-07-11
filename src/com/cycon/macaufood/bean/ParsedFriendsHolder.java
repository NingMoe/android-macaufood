package com.cycon.macaufood.bean;

public class ParsedFriendsHolder {
	
	private String detail; //detail string like this: 29|||Cyrus Lam|||https://graph.facebook.com/686125482/picture|||1
	//need to split detail string to get other properties
	
	private String id;
	private String name;
	private String picLink;
	private boolean followed;
	//for load progressbar and disable button
	private boolean loading;
	

	public boolean isLoading() {
		return loading;
	}

	public void setLoading(boolean loading) {
		this.loading = loading;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPicLink() {
		return picLink;
	}

	public void setPicLink(String picLink) {
		this.picLink = picLink;
	}

	public boolean isFollowed() {
		return followed;
	}

	public void setFollowed(boolean followed) {
		this.followed = followed;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}
	
	

}
