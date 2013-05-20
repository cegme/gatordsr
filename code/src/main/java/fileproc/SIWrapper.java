package fileproc;

import streamcorpus.StreamItem;

class SIWrapper {// extends Serializable {
	String			day;
	int					hour;
	String			fileName;
	int					index;
	StreamItem	streamItem;

	public SIWrapper(String day, int hour, String fileName, int index, StreamItem streamItem) {
		this.day = day;
		this.hour = hour;
		this.fileName = fileName;
		this.index = index;
		this.streamItem = streamItem;
	}

	public void gc() {
		streamItem = null;
	}

	// getDirectoryName(day: String, hour: Int): String = {
	// val hourStr = EmbededFaucet.numberFormatter.format(hour)
	// "%s-%s".format(day, hourStr)
	// }

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public StreamItem getStreamItem() {
		return streamItem;
	}

	public void setStreamItem(StreamItem streamItem) {
		this.streamItem = streamItem;
	}

	public String toString() {
		String str = day + "-" + hour + "/" + fileName + "[" + index + "]";

		// var str = new StringBuilder(getDirectoryName(day, hour) + "/" + fileName
		// + "[" + index + "]||")
		// if (streamItem.getBody != null && streamItem.getBody.getClean_visible()
		// != null) {
		//
		// val doc = streamItem.getBody.getClean_visible()
		// val query = EmbededFaucet.query
		// // println(query)
		// // println(doc)
		// var i = -1
		// var initIndex = 0
		// var endIndex = query.length()
		//
		// while ((i = doc.indexOf(query, i + 1)) != -1) {
		// initIndex = i - 30
		// if (initIndex < 0)
		// initIndex = 0
		// endIndex = i + query.length + 30
		// if (endIndex > doc.length())
		// endIndex = doc.length() - 1
		// str = str.append(doc.substring(initIndex, endIndex)).append( "||")
		// }
		// }
		return str;
	}
}
