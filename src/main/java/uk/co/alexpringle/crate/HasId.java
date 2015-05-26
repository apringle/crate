package uk.co.alexpringle.crate;

public interface HasId
{
    /**
     * @return Id used by Crate to store and retrieve this object.
     */
    String getId();
}
