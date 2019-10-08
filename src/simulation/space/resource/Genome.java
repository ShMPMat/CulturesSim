package simulation.space.resource;

import extra.ProbFunc;

import java.util.ArrayList;
import java.util.List;

public class Genome {
    private String name;
    private boolean isMutable;
    private double size;
    private List<Resource> parts;
    private double spreadProbability;

    private Material _primaryMaterial;
    private int _mutationCount = 0;

    public Genome(String name, List<ResourceIdeal> parts, double size, double spreadProbability, boolean isMutable) {
        this.name = name;
        this.spreadProbability = spreadProbability;
        this.parts = new ArrayList<>(parts);
        this.size = size;
        this.isMutable = isMutable;
        _primaryMaterial = null;
        computePrimaryMaterial();
    }

    Genome(Genome genome) {
        this(genome.name, new ArrayList<>(), genome.size, genome.spreadProbability, genome.isMutable);
        genome.parts.forEach(this::addPart);
    }

    private void computePrimaryMaterial() {
        if (_primaryMaterial == null && !parts.isEmpty()) {
            _primaryMaterial = parts.get(0).resourceCore.getMaterials().get(0);
        }
    }

    public Material getPrimaryMaterial() {
        return _primaryMaterial;
    }

    public String getName() {
        return name;
    }

    public List<Resource> getParts() {
        return parts;
    }

    public double getSize() {
        return size;
    }

    public double getSpreadProbability() {
        return spreadProbability;
    }

    public boolean isMutable() {
        return isMutable;
    }

    void addPart(Resource part) {
        int i = parts.indexOf(part);
        if (i == -1) {
            parts.add(part);
            computePrimaryMaterial();
        } else {
            parts.get(i).addAmount(part.getAmount());
        }
    }

    public void setName(String name) {
        this.name = name;
        for (Resource part : parts) {
            part.resourceCore.legacyPostfix = "_of_" + name; //TODO stupid, will break
        }
    }

    public void setMutable(boolean mutable) {
        isMutable = mutable;
    }

    public void setSpreadProbability(double spreadProbability) {
        this.spreadProbability = spreadProbability;
    }

    public Genome mutate() {
        if (!isMutable()) {
            System.err.println("Method mutate called from non-mutable genome!");
            return null;
        }
        Genome genome = new Genome(this);
        genome.setName(name + "_Mutation" + _mutationCount);
        _mutationCount++;
        for (Resource part : parts) {
            part.amount += ProbFunc.randomInt(2) - 1;
        }
        return genome;
    }
}
