/**
 * Autogenerated by Thrift Compiler (0.9.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package kba;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;

/**
 * ContentItem is the thrift analog of http://trec-kba.org/schemas/v1.0/content-item.json
 * 
 * The JSON version has a 'stages' property that contains descriptions **and also names** of
 * additional properties on the ContentItem. That was overly flexible. Each content-item in the KBA
 * corpus can have a 'cleansed' and 'ner' property. 'cleansed' is generated from 'raw', and 'ner' is
 * generated from 'cleansed.' Generally, 'cleansed' is a tag-stripped version of 'raw', and 'ner' is
 * the output of a named entity recognizer that generates one-word-per-line output.
 * 
 * For the kba-stream-corpus-2012, the specific tag-stripping and NER configurations were: 'raw' -->
 * boilerpipe 1.2.0 ArticleExtractor --> 'cleansed'
 * 
 * 'cleansed' -> Stanford CoreNLP ver 1.2.0 with annotators {tokenize, cleanxml, ssplit, pos, lemma,
 * ner}, property pos.maxlen=100" --> 'ner'
 */
public class ContentItem implements org.apache.thrift.TBase<ContentItem, ContentItem._Fields>, java.io.Serializable,
		Cloneable {
	private static final org.apache.thrift.protocol.TStruct						STRUCT_DESC					= new org.apache.thrift.protocol.TStruct(
																																														"ContentItem");

	private static final org.apache.thrift.protocol.TField						RAW_FIELD_DESC			= new org.apache.thrift.protocol.TField(
																																														"raw",
																																														org.apache.thrift.protocol.TType.STRING,
																																														(short) 1);
	private static final org.apache.thrift.protocol.TField						ENCODING_FIELD_DESC	= new org.apache.thrift.protocol.TField(
																																														"encoding",
																																														org.apache.thrift.protocol.TType.STRING,
																																														(short) 2);
	private static final org.apache.thrift.protocol.TField						CLEANSED_FIELD_DESC	= new org.apache.thrift.protocol.TField(
																																														"cleansed",
																																														org.apache.thrift.protocol.TType.STRING,
																																														(short) 3);
	private static final org.apache.thrift.protocol.TField						NER_FIELD_DESC			= new org.apache.thrift.protocol.TField(
																																														"ner",
																																														org.apache.thrift.protocol.TType.STRING,
																																														(short) 4);

	private static final Map<Class<? extends IScheme>, SchemeFactory>	schemes							= new HashMap<Class<? extends IScheme>, SchemeFactory>();
	static {
		schemes.put(StandardScheme.class, new ContentItemStandardSchemeFactory());
		schemes.put(TupleScheme.class, new ContentItemTupleSchemeFactory());
	}

	public ByteBuffer																									raw;																																					// required
	public String																											encoding;																																		// required
	public ByteBuffer																									cleansed;																																		// optional
	public ByteBuffer																									ner;																																					// optional

	/**
	 * The set of fields this struct contains, along with convenience methods for finding and
	 * manipulating them.
	 */
	public enum _Fields implements org.apache.thrift.TFieldIdEnum {
		RAW((short) 1, "raw"), ENCODING((short) 2, "encoding"), CLEANSED((short) 3, "cleansed"), NER((short) 4, "ner");

		private static final Map<String, _Fields>	byName	= new HashMap<String, _Fields>();

		static {
			for (_Fields field : EnumSet.allOf(_Fields.class)) {
				byName.put(field.getFieldName(), field);
			}
		}

		/**
		 * Find the _Fields constant that matches fieldId, or null if its not found.
		 */
		public static _Fields findByThriftId(int fieldId) {
			switch (fieldId) {
			case 1: // RAW
				return RAW;
			case 2: // ENCODING
				return ENCODING;
			case 3: // CLEANSED
				return CLEANSED;
			case 4: // NER
				return NER;
			default:
				return null;
			}
		}

		/**
		 * Find the _Fields constant that matches fieldId, throwing an exception if it is not found.
		 */
		public static _Fields findByThriftIdOrThrow(int fieldId) {
			_Fields fields = findByThriftId(fieldId);
			if (fields == null)
				throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
			return fields;
		}

		/**
		 * Find the _Fields constant that matches name, or null if its not found.
		 */
		public static _Fields findByName(String name) {
			return byName.get(name);
		}

		private final short		_thriftId;
		private final String	_fieldName;

		_Fields(short thriftId, String fieldName) {
			_thriftId = thriftId;
			_fieldName = fieldName;
		}

		@Override
		public short getThriftFieldId() {
			return _thriftId;
		}

		@Override
		public String getFieldName() {
			return _fieldName;
		}
	}

	// isset id assignments
	private _Fields																															optionals[]	= { _Fields.CLEANSED,
			_Fields.NER																																				};
	public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData>	metaDataMap;
	static {
		Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(
				_Fields.class);
		tmpMap.put(_Fields.RAW, new org.apache.thrift.meta_data.FieldMetaData("raw",
				org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(
						org.apache.thrift.protocol.TType.STRING, true)));
		tmpMap.put(_Fields.ENCODING, new org.apache.thrift.meta_data.FieldMetaData("encoding",
				org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(
						org.apache.thrift.protocol.TType.STRING)));
		tmpMap.put(_Fields.CLEANSED, new org.apache.thrift.meta_data.FieldMetaData("cleansed",
				org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(
						org.apache.thrift.protocol.TType.STRING, true)));
		tmpMap.put(_Fields.NER, new org.apache.thrift.meta_data.FieldMetaData("ner",
				org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(
						org.apache.thrift.protocol.TType.STRING, true)));
		metaDataMap = Collections.unmodifiableMap(tmpMap);
		org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ContentItem.class, metaDataMap);
	}

	public ContentItem() {
	}

	public ContentItem(ByteBuffer raw, String encoding) {
		this();
		this.raw = raw;
		this.encoding = encoding;
	}

	/**
	 * Performs a deep copy on <i>other</i>.
	 */
	public ContentItem(ContentItem other) {
		if (other.isSetRaw()) {
			this.raw = org.apache.thrift.TBaseHelper.copyBinary(other.raw);
			;
		}
		if (other.isSetEncoding()) {
			this.encoding = other.encoding;
		}
		if (other.isSetCleansed()) {
			this.cleansed = org.apache.thrift.TBaseHelper.copyBinary(other.cleansed);
			;
		}
		if (other.isSetNer()) {
			this.ner = org.apache.thrift.TBaseHelper.copyBinary(other.ner);
			;
		}
	}

	@Override
	public ContentItem deepCopy() {
		return new ContentItem(this);
	}

	@Override
	public void clear() {
		this.raw = null;
		this.encoding = null;
		this.cleansed = null;
		this.ner = null;
	}

	public byte[] getRaw() {
		setRaw(org.apache.thrift.TBaseHelper.rightSize(raw));
		return raw == null ? null : raw.array();
	}

	public ByteBuffer bufferForRaw() {
		return raw;
	}

	public ContentItem setRaw(byte[] raw) {
		setRaw(raw == null ? (ByteBuffer) null : ByteBuffer.wrap(raw));
		return this;
	}

	public ContentItem setRaw(ByteBuffer raw) {
		this.raw = raw;
		return this;
	}

	public void unsetRaw() {
		this.raw = null;
	}

	/** Returns true if field raw is set (has been assigned a value) and false otherwise */
	public boolean isSetRaw() {
		return this.raw != null;
	}

	public void setRawIsSet(boolean value) {
		if (!value) {
			this.raw = null;
		}
	}

	public String getEncoding() {
		return this.encoding;
	}

	public ContentItem setEncoding(String encoding) {
		this.encoding = encoding;
		return this;
	}

	public void unsetEncoding() {
		this.encoding = null;
	}

	/** Returns true if field encoding is set (has been assigned a value) and false otherwise */
	public boolean isSetEncoding() {
		return this.encoding != null;
	}

	public void setEncodingIsSet(boolean value) {
		if (!value) {
			this.encoding = null;
		}
	}

	public byte[] getCleansed() {
		setCleansed(org.apache.thrift.TBaseHelper.rightSize(cleansed));
		return cleansed == null ? null : cleansed.array();
	}

	public ByteBuffer bufferForCleansed() {
		return cleansed;
	}

	public ContentItem setCleansed(byte[] cleansed) {
		setCleansed(cleansed == null ? (ByteBuffer) null : ByteBuffer.wrap(cleansed));
		return this;
	}

	public ContentItem setCleansed(ByteBuffer cleansed) {
		this.cleansed = cleansed;
		return this;
	}

	public void unsetCleansed() {
		this.cleansed = null;
	}

	/** Returns true if field cleansed is set (has been assigned a value) and false otherwise */
	public boolean isSetCleansed() {
		return this.cleansed != null;
	}

	public void setCleansedIsSet(boolean value) {
		if (!value) {
			this.cleansed = null;
		}
	}

	public byte[] getNer() {
		setNer(org.apache.thrift.TBaseHelper.rightSize(ner));
		return ner == null ? null : ner.array();
	}

	public ByteBuffer bufferForNer() {
		return ner;
	}

	public ContentItem setNer(byte[] ner) {
		setNer(ner == null ? (ByteBuffer) null : ByteBuffer.wrap(ner));
		return this;
	}

	public ContentItem setNer(ByteBuffer ner) {
		this.ner = ner;
		return this;
	}

	public void unsetNer() {
		this.ner = null;
	}

	/** Returns true if field ner is set (has been assigned a value) and false otherwise */
	public boolean isSetNer() {
		return this.ner != null;
	}

	public void setNerIsSet(boolean value) {
		if (!value) {
			this.ner = null;
		}
	}

	@Override
	public void setFieldValue(_Fields field, Object value) {
		switch (field) {
		case RAW:
			if (value == null) {
				unsetRaw();
			} else {
				setRaw((ByteBuffer) value);
			}
			break;

		case ENCODING:
			if (value == null) {
				unsetEncoding();
			} else {
				setEncoding((String) value);
			}
			break;

		case CLEANSED:
			if (value == null) {
				unsetCleansed();
			} else {
				setCleansed((ByteBuffer) value);
			}
			break;

		case NER:
			if (value == null) {
				unsetNer();
			} else {
				setNer((ByteBuffer) value);
			}
			break;

		}
	}

	@Override
	public Object getFieldValue(_Fields field) {
		switch (field) {
		case RAW:
			return getRaw();

		case ENCODING:
			return getEncoding();

		case CLEANSED:
			return getCleansed();

		case NER:
			return getNer();

		}
		throw new IllegalStateException();
	}

	/**
	 * Returns true if field corresponding to fieldID is set (has been assigned a value) and false
	 * otherwise
	 */
	@Override
	public boolean isSet(_Fields field) {
		if (field == null) {
			throw new IllegalArgumentException();
		}

		switch (field) {
		case RAW:
			return isSetRaw();
		case ENCODING:
			return isSetEncoding();
		case CLEANSED:
			return isSetCleansed();
		case NER:
			return isSetNer();
		}
		throw new IllegalStateException();
	}

	@Override
	public boolean equals(Object that) {
		if (that == null)
			return false;
		if (that instanceof ContentItem)
			return this.equals((ContentItem) that);
		return false;
	}

	public boolean equals(ContentItem that) {
		if (that == null)
			return false;

		boolean this_present_raw = true && this.isSetRaw();
		boolean that_present_raw = true && that.isSetRaw();
		if (this_present_raw || that_present_raw) {
			if (!(this_present_raw && that_present_raw))
				return false;
			if (!this.raw.equals(that.raw))
				return false;
		}

		boolean this_present_encoding = true && this.isSetEncoding();
		boolean that_present_encoding = true && that.isSetEncoding();
		if (this_present_encoding || that_present_encoding) {
			if (!(this_present_encoding && that_present_encoding))
				return false;
			if (!this.encoding.equals(that.encoding))
				return false;
		}

		boolean this_present_cleansed = true && this.isSetCleansed();
		boolean that_present_cleansed = true && that.isSetCleansed();
		if (this_present_cleansed || that_present_cleansed) {
			if (!(this_present_cleansed && that_present_cleansed))
				return false;
			if (!this.cleansed.equals(that.cleansed))
				return false;
		}

		boolean this_present_ner = true && this.isSetNer();
		boolean that_present_ner = true && that.isSetNer();
		if (this_present_ner || that_present_ner) {
			if (!(this_present_ner && that_present_ner))
				return false;
			if (!this.ner.equals(that.ner))
				return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	public int compareTo(ContentItem other) {
		if (!getClass().equals(other.getClass())) {
			return getClass().getName().compareTo(other.getClass().getName());
		}

		int lastComparison = 0;
		ContentItem typedOther = (ContentItem) other;

		lastComparison = Boolean.valueOf(isSetRaw()).compareTo(typedOther.isSetRaw());
		if (lastComparison != 0) {
			return lastComparison;
		}
		if (isSetRaw()) {
			lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.raw, typedOther.raw);
			if (lastComparison != 0) {
				return lastComparison;
			}
		}
		lastComparison = Boolean.valueOf(isSetEncoding()).compareTo(typedOther.isSetEncoding());
		if (lastComparison != 0) {
			return lastComparison;
		}
		if (isSetEncoding()) {
			lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.encoding, typedOther.encoding);
			if (lastComparison != 0) {
				return lastComparison;
			}
		}
		lastComparison = Boolean.valueOf(isSetCleansed()).compareTo(typedOther.isSetCleansed());
		if (lastComparison != 0) {
			return lastComparison;
		}
		if (isSetCleansed()) {
			lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.cleansed, typedOther.cleansed);
			if (lastComparison != 0) {
				return lastComparison;
			}
		}
		lastComparison = Boolean.valueOf(isSetNer()).compareTo(typedOther.isSetNer());
		if (lastComparison != 0) {
			return lastComparison;
		}
		if (isSetNer()) {
			lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.ner, typedOther.ner);
			if (lastComparison != 0) {
				return lastComparison;
			}
		}
		return 0;
	}

	@Override
	public _Fields fieldForId(int fieldId) {
		return _Fields.findByThriftId(fieldId);
	}

	@Override
	public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
		schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
	}

	@Override
	public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
		schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("ContentItem(");
		boolean first = true;

		sb.append("raw:");
		if (this.raw == null) {
			sb.append("null");
		} else {
			org.apache.thrift.TBaseHelper.toString(this.raw, sb);
		}
		first = false;
		if (!first)
			sb.append(", ");
		sb.append("encoding:");
		if (this.encoding == null) {
			sb.append("null");
		} else {
			sb.append(this.encoding);
		}
		first = false;
		if (isSetCleansed()) {
			if (!first)
				sb.append(", ");
			sb.append("cleansed:");
			if (this.cleansed == null) {
				sb.append("null");
			} else {
				org.apache.thrift.TBaseHelper.toString(this.cleansed, sb);
			}
			first = false;
		}
		if (isSetNer()) {
			if (!first)
				sb.append(", ");
			sb.append("ner:");
			if (this.ner == null) {
				sb.append("null");
			} else {
				org.apache.thrift.TBaseHelper.toString(this.ner, sb);
			}
			first = false;
		}
		sb.append(")");
		return sb.toString();
	}

	public void validate() throws org.apache.thrift.TException {
		// check for required fields
		// check for sub-struct validity
	}

	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
		try {
			write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
		} catch (org.apache.thrift.TException te) {
			throw new java.io.IOException(te);
		}
	}

	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
		try {
			read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
		} catch (org.apache.thrift.TException te) {
			throw new java.io.IOException(te);
		}
	}

	private static class ContentItemStandardSchemeFactory implements SchemeFactory {
		@Override
		public ContentItemStandardScheme getScheme() {
			return new ContentItemStandardScheme();
		}
	}

	private static class ContentItemStandardScheme extends StandardScheme<ContentItem> {

		@Override
		public void read(org.apache.thrift.protocol.TProtocol iprot, ContentItem struct)
				throws org.apache.thrift.TException {
			org.apache.thrift.protocol.TField schemeField;
			iprot.readStructBegin();
			while (true) {
				schemeField = iprot.readFieldBegin();
				if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
					break;
				}
				switch (schemeField.id) {
				case 1: // RAW
					if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
						struct.raw = iprot.readBinary();
						struct.setRawIsSet(true);
					} else {
						org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
					}
					break;
				case 2: // ENCODING
					if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
						struct.encoding = iprot.readString();
						struct.setEncodingIsSet(true);
					} else {
						org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
					}
					break;
				case 3: // CLEANSED
					if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
						struct.cleansed = iprot.readBinary();
						struct.setCleansedIsSet(true);
					} else {
						org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
					}
					break;
				case 4: // NER
					if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
						struct.ner = iprot.readBinary();
						struct.setNerIsSet(true);
					} else {
						org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
					}
					break;
				default:
					org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
				}
				iprot.readFieldEnd();
			}
			iprot.readStructEnd();

			// check for required fields of primitive type, which can't be checked in the validate method
			struct.validate();
		}

		@Override
		public void write(org.apache.thrift.protocol.TProtocol oprot, ContentItem struct)
				throws org.apache.thrift.TException {
			struct.validate();

			oprot.writeStructBegin(STRUCT_DESC);
			if (struct.raw != null) {
				oprot.writeFieldBegin(RAW_FIELD_DESC);
				oprot.writeBinary(struct.raw);
				oprot.writeFieldEnd();
			}
			if (struct.encoding != null) {
				oprot.writeFieldBegin(ENCODING_FIELD_DESC);
				oprot.writeString(struct.encoding);
				oprot.writeFieldEnd();
			}
			if (struct.cleansed != null) {
				if (struct.isSetCleansed()) {
					oprot.writeFieldBegin(CLEANSED_FIELD_DESC);
					oprot.writeBinary(struct.cleansed);
					oprot.writeFieldEnd();
				}
			}
			if (struct.ner != null) {
				if (struct.isSetNer()) {
					oprot.writeFieldBegin(NER_FIELD_DESC);
					oprot.writeBinary(struct.ner);
					oprot.writeFieldEnd();
				}
			}
			oprot.writeFieldStop();
			oprot.writeStructEnd();
		}

	}

	private static class ContentItemTupleSchemeFactory implements SchemeFactory {
		@Override
		public ContentItemTupleScheme getScheme() {
			return new ContentItemTupleScheme();
		}
	}

	private static class ContentItemTupleScheme extends TupleScheme<ContentItem> {

		@Override
		public void write(org.apache.thrift.protocol.TProtocol prot, ContentItem struct)
				throws org.apache.thrift.TException {
			TTupleProtocol oprot = (TTupleProtocol) prot;
			BitSet optionals = new BitSet();
			if (struct.isSetRaw()) {
				optionals.set(0);
			}
			if (struct.isSetEncoding()) {
				optionals.set(1);
			}
			if (struct.isSetCleansed()) {
				optionals.set(2);
			}
			if (struct.isSetNer()) {
				optionals.set(3);
			}
			oprot.writeBitSet(optionals, 4);
			if (struct.isSetRaw()) {
				oprot.writeBinary(struct.raw);
			}
			if (struct.isSetEncoding()) {
				oprot.writeString(struct.encoding);
			}
			if (struct.isSetCleansed()) {
				oprot.writeBinary(struct.cleansed);
			}
			if (struct.isSetNer()) {
				oprot.writeBinary(struct.ner);
			}
		}

		@Override
		public void read(org.apache.thrift.protocol.TProtocol prot, ContentItem struct) throws org.apache.thrift.TException {
			TTupleProtocol iprot = (TTupleProtocol) prot;
			BitSet incoming = iprot.readBitSet(4);
			if (incoming.get(0)) {
				struct.raw = iprot.readBinary();
				struct.setRawIsSet(true);
			}
			if (incoming.get(1)) {
				struct.encoding = iprot.readString();
				struct.setEncodingIsSet(true);
			}
			if (incoming.get(2)) {
				struct.cleansed = iprot.readBinary();
				struct.setCleansedIsSet(true);
			}
			if (incoming.get(3)) {
				struct.ner = iprot.readBinary();
				struct.setNerIsSet(true);
			}
		}
	}

}
