package uk.co.alexpringle.crate.test.crates;

import uk.co.alexpringle.crate.HasId;

public class SimpleItem implements HasId
{
    private String id;

    public SimpleItem()
    {

    }

    public SimpleItem(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }
}
