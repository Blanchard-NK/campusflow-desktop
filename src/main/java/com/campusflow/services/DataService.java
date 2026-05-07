package com.campusflow.services;

import com.campusflow.api.ApiClient;
import com.campusflow.api.ApiException;
import com.campusflow.models.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * DataService — fetches entity lists from the REST API.
 *
 * Each {@code fetch*()} method:
 * <ol>
 *   <li>Calls the real API endpoint with the JWT token.</li>
 *   <li>On failure (network / auth), returns the corresponding demo data list.</li>
 * </ol>
 * <p>
 * Supports both plain JSON arrays ({@code [...]}) and Laravel-style paginated
 * responses ({@code { "data": [...] }}).
 * </p>
 */
public class DataService {

    private static DataService instance;

    public static synchronized DataService getInstance() {
        if (instance == null) instance = new DataService();
        return instance;
    }

    private DataService() {}

    private final ApiClient    client = ApiClient.getInstance();
    private final ObjectMapper mapper = client.getMapper();

    // ── Public fetch methods ───────────────────────────────────

    public List<Student> fetchStudents() throws ApiException {
        return parseList(client.get("/api/students"), new TypeReference<>() {});
    }

    public List<Teacher> fetchTeachers() throws ApiException {
        return parseList(client.get("/api/teachers"), new TypeReference<>() {});
    }

    public List<Course> fetchCourses() throws ApiException {
        return parseList(client.get("/api/courses"), new TypeReference<>() {});
    }

    public List<Review> fetchReviews() throws ApiException {
        return parseList(client.get("/api/reviews"), new TypeReference<>() {});
    }

    public DashboardStats fetchDashboardStats() throws ApiException {
        return new DashboardStats(
            fetchStudents(), fetchTeachers(), fetchCourses(), fetchReviews()
        );
    }

    // ── Demo / fallback data ───────────────────────────────────

    public List<Student> getDemoStudents() {
        List<Student> list = new ArrayList<>();
        Object[][] rows = {
            {1,"Emma","Moreau","emma.moreau@etudiant.fr","Master","Intelligence Artificielle","2ème année","Actif"},
            {2,"Lucas","Bernard","lucas.bernard@etudiant.fr","Licence","Informatique","3ème année","Actif"},
            {3,"Chloé","Petit","chloe.petit@etudiant.fr","BTS","SIO SLAM","2ème année","Actif"},
            {4,"Nathan","Simon","nathan.simon@etudiant.fr","Bachelor","Management","1ère année","Actif"},
            {5,"Léa","Laurent","lea.laurent@etudiant.fr","Master","Cybersécurité","1ère année","Actif"},
            {6,"Théo","Lefevre","theo.lefevre@etudiant.fr","Licence","Mathématiques","2ème année","Suspendu"},
            {7,"Inès","Dubois","ines.dubois@etudiant.fr","BTS","MCO","1ère année","Actif"},
            {8,"Antoine","Martinez","antoine.martinez@etudiant.fr","Bachelor","Design UX/UI","2ème année","Actif"},
            {9,"Camille","Garcia","camille.garcia@etudiant.fr","Licence","Économie","3ème année","Actif"},
            {10,"Hugo","Roux","hugo.roux@etudiant.fr","Master","Droit des affaires","2ème année","Diplômé"},
            {11,"Manon","Fournier","manon.fournier@etudiant.fr","BTS","NDRC","2ème année","Actif"},
            {12,"Axel","Henry","axel.henry@etudiant.fr","Bachelor","Finance","3ème année","Actif"},
        };
        for (Object[] r : rows) {
            Student s = new Student();
            s.setId((int)r[0]); s.setFirstName((String)r[1]); s.setLastName((String)r[2]);
            s.setEmail((String)r[3]); s.setFormation((String)r[4]); s.setSpeciality((String)r[5]);
            s.setYear((String)r[6]); s.setStatus((String)r[7]);
            list.add(s);
        }
        return list;
    }

    public List<Teacher> getDemoTeachers() {
        List<Teacher> list = new ArrayList<>();
        Object[][] rows = {
            {1,"Sophie","Renard","sophie.renard@campus.fr","Informatique","BTS, Licence","Titulaire",18},
            {2,"Marc","Dupuis","marc.dupuis@campus.fr","Mathématiques","BTS, Bachelor","Titulaire",18},
            {3,"Claire","Bonnet","claire.bonnet@campus.fr","Économie","Licence, Master","Titulaire",16},
            {4,"Julien","Colin","julien.colin@campus.fr","Langues","Tous niveaux","Contractuel",12},
            {5,"Nathalie","Girard","nathalie.girard@campus.fr","Management","Bachelor, Master","Titulaire",18},
            {6,"Pierre","Lambert","pierre.lambert@campus.fr","Sciences","BTS, Licence","Vacataire",8},
            {7,"Isabelle","Morin","isabelle.morin@campus.fr","Informatique","Master","Titulaire",18},
            {8,"David","Rousseau","david.rousseau@campus.fr","Mathématiques","Licence, Master","Contractuel",14},
        };
        for (Object[] r : rows) {
            Teacher t = new Teacher();
            t.setId((int)r[0]); t.setFirstName((String)r[1]); t.setLastName((String)r[2]);
            t.setEmail((String)r[3]); t.setSpeciality((String)r[4]); t.setFormations((String)r[5]);
            t.setStatus((String)r[6]); t.setHoursPerWeek((int)r[7]);
            list.add(t);
        }
        return list;
    }

    public List<Course> getDemoCourses() {
        List<Course> list = new ArrayList<>();
        Object[][] rows = {
            {1,"BTS SIO SLAM","BTS","Informatique",52,60,"2 ans",12.8},
            {2,"BTS MCO","BTS","Commerce",48,55,"2 ans",11.4},
            {3,"BTS NDRC","BTS","Commercial",44,50,"2 ans",10.9},
            {4,"Bachelor Dev. Web","Bachelor","Informatique",38,40,"3 ans",14.2},
            {5,"Bachelor Finance","Bachelor","Finance",32,40,"3 ans",13.1},
            {6,"Licence Informatique","Licence","Informatique",95,100,"3 ans",13.7},
            {7,"Licence Économie","Licence","Économie",80,90,"3 ans",12.1},
            {8,"Master IA","Master","Informatique",28,30,"2 ans",16.5},
            {9,"Master Cybersec.","Master","Sécurité",24,30,"2 ans",15.8},
            {10,"Master Management","Master","Gestion",20,25,"2 ans",14.6},
        };
        for (Object[] r : rows) {
            Course c = new Course();
            c.setId((int)r[0]); c.setName((String)r[1]); c.setLevel((String)r[2]);
            c.setDomain((String)r[3]); c.setEnrolled((int)r[4]); c.setCapacity((int)r[5]);
            c.setDuration((String)r[6]); c.setAverageGrade((double)r[7]);
            list.add(c);
        }
        return list;
    }

    public List<Review> getDemoReviews() {
        List<Review> list = new ArrayList<>();
        Object[][] rows = {
            {1,1,"Emma Moreau",8,"Master IA",4.8,"Excellent cours, très bien structuré","2024-10-15"},
            {2,2,"Lucas Bernard",6,"Licence Informatique",3.5,"Contenu intéressant mais dense","2024-10-18"},
            {3,3,"Chloé Petit",1,"BTS SIO SLAM",4.2,"Bonne ambiance, prof disponible","2024-10-20"},
            {4,4,"Nathan Simon",5,"Bachelor Finance",4.0,"Cours clair et bien organisé","2024-11-02"},
            {5,5,"Léa Laurent",9,"Master Cybersec.",4.9,"Parfait pour notre niveau","2024-11-05"},
            {6,7,"Inès Dubois",2,"BTS MCO",2.8,"Trop d'informations en peu de temps","2024-11-08"},
            {7,8,"Antoine Martinez",4,"Bachelor Dev. Web",4.5,"Très pratique, j'ai beaucoup appris","2024-11-10"},
            {8,9,"Camille Garcia",7,"Licence Économie",3.8,"Contenu solide, manque d'exemples","2024-11-12"},
            {9,1,"Emma Moreau",9,"Master Cybersec.",4.7,"Excellente pédagogie","2024-11-14"},
            {10,2,"Lucas Bernard",8,"Master IA",4.1,"Très bon cours avancé","2024-11-15"},
            {11,11,"Manon Fournier",3,"BTS NDRC",3.2,"Correct, peut mieux faire","2024-11-18"},
            {12,12,"Axel Henry",5,"Bachelor Finance",4.3,"Bon équilibre théorie/pratique","2024-11-20"},
        };
        for (Object[] r : rows) {
            Review rv = new Review();
            rv.setId((int)r[0]); rv.setStudentId((int)r[1]); rv.setStudentName((String)r[2]);
            rv.setCourseId((int)r[3]); rv.setCourseName((String)r[4]); rv.setRating((double)r[5]);
            rv.setComment((String)r[6]); rv.setCreatedAt((String)r[7]);
            list.add(rv);
        }
        return list;
    }

    public DashboardStats getDemoDashboardStats() {
        return new DashboardStats(
            getDemoStudents(), getDemoTeachers(), getDemoCourses(), getDemoReviews()
        );
    }

    // ── JSON helpers ──────────────────────────────────────────

    /**
     * Parses a JSON string that is either a plain array or a Laravel-paginated
     * object {@code { "data": [...] }}.
     */
    private <T> List<T> parseList(String json, TypeReference<List<T>> ref) throws ApiException {
        try {
            JsonNode root = mapper.readTree(json);
            JsonNode arrayNode = root.isObject() && root.has("data") ? root.get("data") : root;
            return mapper.convertValue(arrayNode, ref);
        } catch (Exception e) {
            throw new ApiException("JSON parse error: " + e.getMessage(), 0, e);
        }
    }
}
