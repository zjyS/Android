
package com.example.myMusic;

/**
 * @author ZJY
 *自定义类，用于保存每一首歌信息
 */
public class MediaResource {
	private int _id ;
	private String _title;
	private String _musician ;
	private String _article;
	private String _datapath;
	private long _size;
	private int _duration;
	public MediaResource(int _id,String _title,String _musician,String _article,String _datapath,long _size,int _duration){
		this._id = _id;
		this._title = _title;
		this._musician = _musician;
		this._article = _article;
		this._datapath = _datapath;
		this._size = _size;
		this._duration = _duration;
		}
	
	public void setId(int _id){
		this._id = _id;
	}
	
	public void setTitle(String _title){
		this._title = _title;
	}
	public void setMusician(String _musician){
		this._musician = _musician;
	}
	
	public void setArticle(String _article){
		this._article = _article;
	}
	
	public void setDatapath(String _datapath){
		this._datapath = _datapath;
	}
	
	public void setSize(long _size){
		this._size = _size;
	}
	
	public void setDuratioin(int _duration){
		this._duration = _duration;
	}
	public int getId(){
		return _id;
	}
	
	public String getTitle(){
		return _title;
	}
	
	public String getMusician(){
		return _musician;
	}
	
	public String getArticle(){
		return _article;
	}
	
	public String getDatapath(){
		return _datapath;
	}
	
	public long getSize(){
		return _size;
	}
	
	public int getDuration(){
		return _duration;
	}
	
}
