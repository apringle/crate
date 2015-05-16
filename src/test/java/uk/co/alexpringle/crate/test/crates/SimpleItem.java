package uk.co.alexpringle.crate.test.crates;

import uk.co.alexpringle.crate.HasId;

public class SimpleItem implements HasId
{
    private String id;
    private String name;
    private String email;
    private Double balance;
    private int someInt;
    private long someLong;
    private float someFloat;
    private boolean enabled;

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

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public Double getBalance()
    {
        return balance;
    }

    public void setBalance(Double balance)
    {
        this.balance = balance;
    }

    public int getSomeInt()
    {
        return someInt;
    }

    public void setSomeInt(int someInt)
    {
        this.someInt = someInt;
    }

    public long getSomeLong()
    {
        return someLong;
    }

    public void setSomeLong(long someLong)
    {
        this.someLong = someLong;
    }

    public float getSomeFloat()
    {
        return someFloat;
    }

    public void setSomeFloat(float someFloat)
    {
        this.someFloat = someFloat;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        SimpleItem that = (SimpleItem) o;

        if (someInt != that.someInt)
        {
            return false;
        }
        if (someLong != that.someLong)
        {
            return false;
        }
        if (Float.compare(that.someFloat, someFloat) != 0)
        {
            return false;
        }
        if (enabled != that.enabled)
        {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null)
        {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null)
        {
            return false;
        }
        if (email != null ? !email.equals(that.email) : that.email != null)
        {
            return false;
        }
        return !(balance != null ? !balance.equals(that.balance) : that.balance != null);

    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (balance != null ? balance.hashCode() : 0);
        result = 31 * result + someInt;
        result = 31 * result + (int) (someLong ^ (someLong >>> 32));
        result = 31 * result + (someFloat != +0.0f ? Float.floatToIntBits(someFloat) : 0);
        result = 31 * result + (enabled ? 1 : 0);
        return result;
    }
}
