package uk.co.alexpringle.crate.test.crates;

import android.content.Context;
import uk.co.alexpringle.crate.Crate;
import uk.co.alexpringle.crate.HasId;

public class SimpleCrate extends Crate<SimpleItem>
{
    public SimpleCrate(Context context)
    {
        super(context);
    }
}

class SimpleItem implements HasId
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