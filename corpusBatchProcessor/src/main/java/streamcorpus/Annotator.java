/**
 * Autogenerated by Thrift Compiler (1.0.0-dev)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package streamcorpus;

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

/**
 * An Annotator object describes a human (or possibly a set of humans)
 * who generated the data stored in a Label or Rating object.
 */
public class Annotator implements org.apache.thrift.TBase<Annotator, Annotator._Fields>, java.io.Serializable, Cloneable, Comparable<Annotator> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("Annotator");

  private static final org.apache.thrift.protocol.TField ANNOTATOR_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("annotator_id", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField ANNOTATION_TIME_FIELD_DESC = new org.apache.thrift.protocol.TField("annotation_time", org.apache.thrift.protocol.TType.STRUCT, (short)2);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new AnnotatorStandardSchemeFactory());
    schemes.put(TupleScheme.class, new AnnotatorTupleSchemeFactory());
  }

  public String annotator_id; // required
  /**
   * Approximate time when annotations/judgments/labels was rendered
   * by human.  If this is missing, it means that the time was not
   * recorded, which often happens when the author made the
   * annotation.
   */
  public StreamTime annotation_time; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    ANNOTATOR_ID((short)1, "annotator_id"),
    /**
     * Approximate time when annotations/judgments/labels was rendered
     * by human.  If this is missing, it means that the time was not
     * recorded, which often happens when the author made the
     * annotation.
     */
    ANNOTATION_TIME((short)2, "annotation_time");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // ANNOTATOR_ID
          return ANNOTATOR_ID;
        case 2: // ANNOTATION_TIME
          return ANNOTATION_TIME;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

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
  private _Fields optionals[] = {_Fields.ANNOTATION_TIME};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.ANNOTATOR_ID, new org.apache.thrift.meta_data.FieldMetaData("annotator_id", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , "AnnotatorID")));
    tmpMap.put(_Fields.ANNOTATION_TIME, new org.apache.thrift.meta_data.FieldMetaData("annotation_time", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, StreamTime.class)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(Annotator.class, metaDataMap);
  }

  public Annotator() {
  }

  public Annotator(
    String annotator_id)
  {
    this();
    this.annotator_id = annotator_id;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public Annotator(Annotator other) {
    if (other.isSetAnnotator_id()) {
      this.annotator_id = other.annotator_id;
    }
    if (other.isSetAnnotation_time()) {
      this.annotation_time = new StreamTime(other.annotation_time);
    }
  }

  @Override
public Annotator deepCopy() {
    return new Annotator(this);
  }

  @Override
  public void clear() {
    this.annotator_id = null;
    this.annotation_time = null;
  }

  public String getAnnotator_id() {
    return this.annotator_id;
  }

  public Annotator setAnnotator_id(String annotator_id) {
    this.annotator_id = annotator_id;
    return this;
  }

  public void unsetAnnotator_id() {
    this.annotator_id = null;
  }

  /** Returns true if field annotator_id is set (has been assigned a value) and false otherwise */
  public boolean isSetAnnotator_id() {
    return this.annotator_id != null;
  }

  public void setAnnotator_idIsSet(boolean value) {
    if (!value) {
      this.annotator_id = null;
    }
  }

  /**
   * Approximate time when annotations/judgments/labels was rendered
   * by human.  If this is missing, it means that the time was not
   * recorded, which often happens when the author made the
   * annotation.
   */
  public StreamTime getAnnotation_time() {
    return this.annotation_time;
  }

  /**
   * Approximate time when annotations/judgments/labels was rendered
   * by human.  If this is missing, it means that the time was not
   * recorded, which often happens when the author made the
   * annotation.
   */
  public Annotator setAnnotation_time(StreamTime annotation_time) {
    this.annotation_time = annotation_time;
    return this;
  }

  public void unsetAnnotation_time() {
    this.annotation_time = null;
  }

  /** Returns true if field annotation_time is set (has been assigned a value) and false otherwise */
  public boolean isSetAnnotation_time() {
    return this.annotation_time != null;
  }

  public void setAnnotation_timeIsSet(boolean value) {
    if (!value) {
      this.annotation_time = null;
    }
  }

  @Override
public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case ANNOTATOR_ID:
      if (value == null) {
        unsetAnnotator_id();
      } else {
        setAnnotator_id((String)value);
      }
      break;

    case ANNOTATION_TIME:
      if (value == null) {
        unsetAnnotation_time();
      } else {
        setAnnotation_time((StreamTime)value);
      }
      break;

    }
  }

  @Override
public Object getFieldValue(_Fields field) {
    switch (field) {
    case ANNOTATOR_ID:
      return getAnnotator_id();

    case ANNOTATION_TIME:
      return getAnnotation_time();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  @Override
public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case ANNOTATOR_ID:
      return isSetAnnotator_id();
    case ANNOTATION_TIME:
      return isSetAnnotation_time();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof Annotator)
      return this.equals((Annotator)that);
    return false;
  }

  public boolean equals(Annotator that) {
    if (that == null)
      return false;

    boolean this_present_annotator_id = true && this.isSetAnnotator_id();
    boolean that_present_annotator_id = true && that.isSetAnnotator_id();
    if (this_present_annotator_id || that_present_annotator_id) {
      if (!(this_present_annotator_id && that_present_annotator_id))
        return false;
      if (!this.annotator_id.equals(that.annotator_id))
        return false;
    }

    boolean this_present_annotation_time = true && this.isSetAnnotation_time();
    boolean that_present_annotation_time = true && that.isSetAnnotation_time();
    if (this_present_annotation_time || that_present_annotation_time) {
      if (!(this_present_annotation_time && that_present_annotation_time))
        return false;
      if (!this.annotation_time.equals(that.annotation_time))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public int compareTo(Annotator other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetAnnotator_id()).compareTo(other.isSetAnnotator_id());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetAnnotator_id()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.annotator_id, other.annotator_id);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetAnnotation_time()).compareTo(other.isSetAnnotation_time());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetAnnotation_time()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.annotation_time, other.annotation_time);
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
    StringBuilder sb = new StringBuilder("Annotator(");
    boolean first = true;

    sb.append("annotator_id:");
    if (this.annotator_id == null) {
      sb.append("null");
    } else {
      sb.append(this.annotator_id);
    }
    first = false;
    if (isSetAnnotation_time()) {
      if (!first) sb.append(", ");
      sb.append("annotation_time:");
      if (this.annotation_time == null) {
        sb.append("null");
      } else {
        sb.append(this.annotation_time);
      }
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
    if (annotation_time != null) {
      annotation_time.validate();
    }
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

  private static class AnnotatorStandardSchemeFactory implements SchemeFactory {
    @Override
	public AnnotatorStandardScheme getScheme() {
      return new AnnotatorStandardScheme();
    }
  }

  private static class AnnotatorStandardScheme extends StandardScheme<Annotator> {

    @Override
	public void read(org.apache.thrift.protocol.TProtocol iprot, Annotator struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // ANNOTATOR_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.annotator_id = iprot.readString();
              struct.setAnnotator_idIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // ANNOTATION_TIME
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.annotation_time = new StreamTime();
              struct.annotation_time.read(iprot);
              struct.setAnnotation_timeIsSet(true);
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
	public void write(org.apache.thrift.protocol.TProtocol oprot, Annotator struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.annotator_id != null) {
        oprot.writeFieldBegin(ANNOTATOR_ID_FIELD_DESC);
        oprot.writeString(struct.annotator_id);
        oprot.writeFieldEnd();
      }
      if (struct.annotation_time != null) {
        if (struct.isSetAnnotation_time()) {
          oprot.writeFieldBegin(ANNOTATION_TIME_FIELD_DESC);
          struct.annotation_time.write(oprot);
          oprot.writeFieldEnd();
        }
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class AnnotatorTupleSchemeFactory implements SchemeFactory {
    @Override
	public AnnotatorTupleScheme getScheme() {
      return new AnnotatorTupleScheme();
    }
  }

  private static class AnnotatorTupleScheme extends TupleScheme<Annotator> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, Annotator struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetAnnotator_id()) {
        optionals.set(0);
      }
      if (struct.isSetAnnotation_time()) {
        optionals.set(1);
      }
      oprot.writeBitSet(optionals, 2);
      if (struct.isSetAnnotator_id()) {
        oprot.writeString(struct.annotator_id);
      }
      if (struct.isSetAnnotation_time()) {
        struct.annotation_time.write(oprot);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, Annotator struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(2);
      if (incoming.get(0)) {
        struct.annotator_id = iprot.readString();
        struct.setAnnotator_idIsSet(true);
      }
      if (incoming.get(1)) {
        struct.annotation_time = new StreamTime();
        struct.annotation_time.read(iprot);
        struct.setAnnotation_timeIsSet(true);
      }
    }
  }

}

